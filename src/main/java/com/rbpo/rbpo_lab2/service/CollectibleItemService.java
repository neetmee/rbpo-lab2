package com.rbpo.rbpo_lab2.service;

import java.util.List;
import org.springframework.stereotype.Service;
import com.rbpo.rbpo_lab2.exception.ApiException;
import com.rbpo.rbpo_lab2.model.AuctionLot;
import com.rbpo.rbpo_lab2.model.CollectibleItem;
import com.rbpo.rbpo_lab2.model.LotStatus;
import com.rbpo.rbpo_lab2.repository.AuctionLotRepository;
import com.rbpo.rbpo_lab2.repository.CollectibleItemRepository;
import com.rbpo.rbpo_lab2.repository.UserRepository;

@Service
public class CollectibleItemService {
    private final CollectibleItemRepository items;
    private final UserRepository users;
    private final AuctionLotRepository lotRepo;
    private final AuctionLotService lots;

    public CollectibleItemService(CollectibleItemRepository items, UserRepository users,
                                  AuctionLotRepository lotRepo, AuctionLotService lots) {
        this.items = items;
        this.users = users;
        this.lotRepo = lotRepo;
        this.lots = lots;
    }

    public List<CollectibleItem> getAll() { return items.findAll(); }

    public List<CollectibleItem> findByOwner(Long ownerId) { return items.findByOwnerId(ownerId); }

    public CollectibleItem getById(Long id) {
        return items.findById(id).orElseThrow(() -> ApiException.notFound("Предмет", id));
    }

    public CollectibleItem create(String name, String description, Long ownerId, String condition) {
        if (name == null || name.isBlank()) throw ApiException.badRequest("Поле name обязательно");
        if (ownerId == null) throw ApiException.badRequest("Поле ownerId обязательно");
        if (!users.existsById(ownerId)) throw ApiException.badRequest("Владелец с id=" + ownerId + " не существует");
        return items.save(new CollectibleItem(items.nextId(), name, description, ownerId, condition));
    }

    public CollectibleItem update(Long id, String name, String description, Long ownerId, String condition) {
        CollectibleItem old = getById(id);
        if (name != null && name.isBlank()) throw ApiException.badRequest("Поле name не может быть пустым");
        if (ownerId != null) {
            if (!users.existsById(ownerId)) throw ApiException.badRequest("Владелец с id=" + ownerId + " не существует");
            if (!ownerId.equals(old.ownerId())
                    && lotRepo.findByItemId(id).stream().anyMatch(l -> l.status() == LotStatus.ACTIVE)) {
                throw ApiException.conflict("Нельзя сменить владельца предмета, выставленного на активный лот");
            }
        }
        return items.save(new CollectibleItem(id,
                name != null ? name : old.name(),
                description != null ? description : old.description(),
                ownerId != null ? ownerId : old.ownerId(),
                condition != null ? condition : old.condition()));
    }

    public void delete(Long id) {
        getById(id);
        for (AuctionLot lot : lotRepo.findByItemId(id)) {
            lots.delete(lot.id());
        }
        items.deleteById(id);
    }
}
