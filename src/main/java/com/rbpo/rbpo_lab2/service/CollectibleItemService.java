package com.rbpo.rbpo_lab2.service;

import com.rbpo.rbpo_lab2.exception.ApiException;
import com.rbpo.rbpo_lab2.model.AuctionLot;
import com.rbpo.rbpo_lab2.model.CollectibleItem;
import com.rbpo.rbpo_lab2.model.LotStatus;
import com.rbpo.rbpo_lab2.model.User;
import com.rbpo.rbpo_lab2.repository.AuctionLotRepository;
import com.rbpo.rbpo_lab2.repository.CollectibleItemRepository;
import com.rbpo.rbpo_lab2.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class CollectibleItemService {

    private final CollectibleItemRepository items;
    private final UserRepository users;
    private final AuctionLotRepository lotRepo;
    private final AuctionLotService lots;

    public CollectibleItemService(CollectibleItemRepository items,
                                  UserRepository users,
                                  AuctionLotRepository lotRepo,
                                  AuctionLotService lots) {
        this.items = items;
        this.users = users;
        this.lotRepo = lotRepo;
        this.lots = lots;
    }

    @Transactional(readOnly = true)
    public List<CollectibleItem> getAll() {
        return items.findAll();
    }

    @Transactional(readOnly = true)
    public List<CollectibleItem> findByOwner(Long ownerId) {
        return items.findAllByOwnerId(ownerId);
    }

    @Transactional(readOnly = true)
    public CollectibleItem getById(Long id) {
        return items.findById(id)
                .orElseThrow(() -> ApiException.notFound("Предмет", id));
    }

    public CollectibleItem create(String name, String description, Long ownerId, String condition) {
        if (name == null || name.isBlank()) {
            throw ApiException.badRequest("Поле name обязательно");
        }
        if (ownerId == null) {
            throw ApiException.badRequest("Поле ownerId обязательно");
        }

        User owner = users.findById(ownerId)
                .orElseThrow(() -> ApiException.badRequest("Владелец с id=" + ownerId + " не существует"));

        String safeCondition = (condition != null && !condition.isBlank()) ? condition : "не указано";
        return items.save(new CollectibleItem(name, description, owner, safeCondition));
    }

    public CollectibleItem update(Long id, String name, String description, Long ownerId, String condition) {
        CollectibleItem item = getById(id);

        if (name != null && name.isBlank()) {
            throw ApiException.badRequest("Поле name не может быть пустым");
        }

        if (ownerId != null) {
            User newOwner = users.findById(ownerId)
                    .orElseThrow(() -> ApiException.badRequest("Владелец с id=" + ownerId + " не существует"));

            if (!ownerId.equals(item.getOwner().getId())
                    && lotRepo.existsByItemIdAndStatusIn(id, List.of(LotStatus.CREATED, LotStatus.ACTIVE))) {
                throw ApiException.conflict("Нельзя сменить владельца предмета, выставленного на аукцион");
            }
            item.setOwner(newOwner);
        }

        if (name != null) item.setName(name);
        if (description != null) item.setDescription(description);
        if (condition != null) item.setCondition(condition);

        return items.save(item);
    }

    public void delete(Long id) {
        CollectibleItem item = getById(id);
        for (AuctionLot lot : lotRepo.findAllByItemId(id)) {
            lots.delete(lot.getId());
        }
        items.delete(item);
    }
}