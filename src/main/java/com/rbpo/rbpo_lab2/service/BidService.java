package com.rbpo.rbpo_lab2.service;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import org.springframework.stereotype.Service;
import com.rbpo.rbpo_lab2.exception.ApiException;
import com.rbpo.rbpo_lab2.model.AuctionLot;
import com.rbpo.rbpo_lab2.model.Bid;
import com.rbpo.rbpo_lab2.model.CollectibleItem;
import com.rbpo.rbpo_lab2.model.LotStatus;
import com.rbpo.rbpo_lab2.repository.AuctionLotRepository;
import com.rbpo.rbpo_lab2.repository.BidRepository;
import com.rbpo.rbpo_lab2.repository.CollectibleItemRepository;
import com.rbpo.rbpo_lab2.repository.UserRepository;

@Service
public class BidService {
    private final BidRepository bids;
    private final AuctionLotRepository lots;
    private final CollectibleItemRepository items;
    private final UserRepository users;
    private final AuctionDealService deals;

    public BidService(BidRepository bids, AuctionLotRepository lots, CollectibleItemRepository items,
                      UserRepository users, AuctionDealService deals) {
        this.bids = bids;
        this.lots = lots;
        this.items = items;
        this.users = users;
        this.deals = deals;
    }

    public List<Bid> getAll() { return bids.findAll(); }

    public Bid getById(Long id) {
        return bids.findById(id).orElseThrow(() -> ApiException.notFound("Ставка", id));
    }

    public Bid create(Long lotId, Long userId, BigDecimal amount) {
        if (lotId == null || userId == null || amount == null) {
            throw ApiException.badRequest("Поля lotId, userId, amount обязательны");
        }
        AuctionLot lot = lots.findById(lotId)
                .orElseThrow(() -> ApiException.badRequest("Лот с id=" + lotId + " не существует"));
        if (!users.existsById(userId)) throw ApiException.badRequest("Пользователь с id=" + userId + " не существует");

        Instant now = Instant.now(); // время задаёт сервер
        checkLotOpen(lot, now);

        CollectibleItem item = items.findById(lot.itemId()).orElseThrow();
        if (item.ownerId().equals(userId)) {
            throw ApiException.conflict("Владелец не может делать ставки на свой лот");
        }
        checkAmount(lot, amount, null);
        return bids.save(new Bid(bids.nextId(), lotId, userId, amount, now));
    }

    public Bid update(Long id, BigDecimal amount) {
        Bid old = getById(id);
        if (amount == null) return old;
        AuctionLot lot = lots.findById(old.lotId()).orElseThrow();
        checkLotOpen(lot, Instant.now());
        if (!deals.findByBid(id).isEmpty()) throw ApiException.conflict("По ставке уже есть сделка");
        checkAmount(lot, amount, id);
        return bids.save(new Bid(id, old.lotId(), old.userId(), amount, old.createdAt()));
    }

    public void delete(Long id) {
        getById(id);
        deals.deleteByBid(id);
        bids.deleteById(id);
    }

    public void deleteByLot(Long lotId) {
        bids.findByLotId(lotId).forEach(b -> delete(b.id()));
    }

    public void deleteByUser(Long userId) {
        bids.findByUserId(userId).forEach(b -> delete(b.id()));
    }

    private static void checkLotOpen(AuctionLot lot, Instant now) {
        if (lot.status() != LotStatus.ACTIVE) throw ApiException.conflict("Лот не активен");
        if (now.isBefore(lot.startTime())) throw ApiException.conflict("Торги по лоту ещё не начались");
        if (now.isAfter(lot.endTime())) throw ApiException.conflict("Торги по лоту завершены");
    }

    /** Ставка должна быть >= стартовой цены или >= (макс. ставка + шаг). */
    private void checkAmount(AuctionLot lot, BigDecimal amount, Long excludeBidId) {
        BigDecimal max = bids.findByLotId(lot.id()).stream()
                .filter(b -> !b.id().equals(excludeBidId))
                .map(Bid::amount)
                .max(BigDecimal::compareTo)
                .orElse(null);
        BigDecimal min = (max == null) ? lot.startPrice() : max.add(lot.bidStep());
        if (amount.compareTo(min) < 0) {
            throw ApiException.badRequest("Минимально допустимая ставка: " + min);
        }
    }
}
