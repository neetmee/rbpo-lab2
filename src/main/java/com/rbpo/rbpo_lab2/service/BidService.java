package com.rbpo.rbpo_lab2.service;

import com.rbpo.rbpo_lab2.exception.ApiException;
import com.rbpo.rbpo_lab2.model.AuctionLot;
import com.rbpo.rbpo_lab2.model.Bid;
import com.rbpo.rbpo_lab2.model.LotStatus;
import com.rbpo.rbpo_lab2.model.User;
import com.rbpo.rbpo_lab2.repository.AuctionDealRepository;
import com.rbpo.rbpo_lab2.repository.AuctionLotRepository;
import com.rbpo.rbpo_lab2.repository.BidRepository;
import com.rbpo.rbpo_lab2.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Objects;

@Service
@Transactional
public class BidService {

    private final BidRepository bids;
    private final AuctionLotRepository lots;
    private final UserRepository users;
    private final AuctionDealRepository deals;

    public BidService(BidRepository bids,
                      AuctionLotRepository lots,
                      UserRepository users,
                      AuctionDealRepository deals) {
        this.bids = bids;
        this.lots = lots;
        this.users = users;
        this.deals = deals;
    }

    @Transactional(readOnly = true)
    public List<Bid> getAll() {
        return bids.findAll();
    }

    @Transactional(readOnly = true)
    public Bid getById(Long id) {
        return bids.findById(id)
                .orElseThrow(() -> ApiException.notFound("Ставка", id));
    }

    public Bid create(Long lotId, Long userId, BigDecimal amount) {
        if (lotId == null || userId == null || amount == null) {
            throw ApiException.badRequest("Поля lotId, userId, amount обязательны");
        }

        AuctionLot lot = lots.findById(lotId)
                .orElseThrow(() -> ApiException.badRequest("Лот с id=" + lotId + " не существует"));

        User user = users.findById(userId)
                .orElseThrow(() -> ApiException.badRequest("Пользователь с id=" + userId + " не существует"));

        Instant now = Instant.now(); // Время задаёт сервер
        checkLotOpen(lot, now);

        if (lot.getItem().getOwner().getId().equals(userId)) {
            throw ApiException.conflict("Владелец не может делать ставки на свой лот");
        }

        checkAmount(lot, amount, null);
        return bids.save(new Bid(lot, user, amount, now));
    }

    public Bid update(Long id, BigDecimal amount) {
        Bid bid = getById(id);
        if (amount == null) return bid;

        AuctionLot lot = bid.getLot();
        checkLotOpen(lot, Instant.now());

        // Проверяем, не оформлена ли сделка по этой ставке через репозиторий
        if (deals.findByLotId(lot.getId()).map(d -> d.getBid().getId().equals(id)).orElse(false)) {
            throw ApiException.conflict("По ставке уже сформирована сделка");
        }

        checkAmount(lot, amount, id);
        bid.setAmount(amount);
        return bids.save(bid);
    }

    public void delete(Long id) {
        Bid bid = getById(id);
        deals.deleteByLotId(bid.getLot().getId());
        bids.delete(bid);
    }

    public void deleteByLot(Long lotId) {
        bids.findAllByLotIdOrderByAmountDesc(lotId).forEach(b -> bids.delete(b));
    }

    public void deleteByUser(Long userId) {
        bids.findAllByUserId(userId).forEach(b -> delete(b.getId()));
    }

    private static void checkLotOpen(AuctionLot lot, Instant now) {
        if (lot.getStatus() != LotStatus.ACTIVE) {
            throw ApiException.conflict("Лот не активен");
        }
        if (now.isBefore(lot.getStartTime())) {
            throw ApiException.conflict("Торги по лоту ещё не начались");
        }
        if (now.isAfter(lot.getEndTime())) {
            throw ApiException.conflict("Торги по лоту завершены");
        }
    }

    /**
     * Ставка должна быть >= стартовой цены или >= (максимальная ставка + шаг аукциона).
     */
    private void checkAmount(AuctionLot lot, BigDecimal amount, Long excludeBidId) {
        BigDecimal max = bids.findAllByLotIdOrderByAmountDesc(lot.getId()).stream()
                .filter(b -> !Objects.equals(b.getId(), excludeBidId))
                .map(Bid::getAmount)
                .findFirst()
                .orElse(null);

        BigDecimal min = (max == null) ? lot.getStartPrice() : max.add(lot.getBidStep());
        if (amount.compareTo(min) < 0) {
            throw ApiException.badRequest("Минимально допустимая ставка: " + min);
        }
    }
}