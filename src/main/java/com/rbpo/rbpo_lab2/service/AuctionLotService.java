package com.rbpo.rbpo_lab2.service;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import org.springframework.stereotype.Service;
import com.rbpo.rbpo_lab2.exception.ApiException;
import com.rbpo.rbpo_lab2.model.AuctionLot;
import com.rbpo.rbpo_lab2.model.LotStatus;
import com.rbpo.rbpo_lab2.repository.AuctionLotRepository;
import com.rbpo.rbpo_lab2.repository.BidRepository;
import com.rbpo.rbpo_lab2.repository.CollectibleItemRepository;

@Service
public class AuctionLotService {
    private final AuctionLotRepository lots;
    private final CollectibleItemRepository items;
    private final BidRepository bidRepo;
    private final BidService bids;
    private final AuctionDealService deals;

    public AuctionLotService(AuctionLotRepository lots, CollectibleItemRepository items,
                             BidRepository bidRepo, BidService bids, AuctionDealService deals) {
        this.lots = lots;
        this.items = items;
        this.bidRepo = bidRepo;
        this.bids = bids;
        this.deals = deals;
    }

    public List<AuctionLot> getAll() { return lots.findAll(); }

    public AuctionLot getById(Long id) {
        return lots.findById(id).orElseThrow(() -> ApiException.notFound("Лот", id));
    }

    public AuctionLot create(Long itemId, BigDecimal startPrice, BigDecimal bidStep,
                             Instant startTime, Instant endTime) {
        if (itemId == null || startPrice == null || bidStep == null || startTime == null || endTime == null) {
            throw ApiException.badRequest("Поля itemId, startPrice, bidStep, startTime, endTime обязательны");
        }
        if (!items.existsById(itemId)) throw ApiException.badRequest("Предмет с id=" + itemId + " не существует");
        validateMoneyAndTime(startPrice, bidStep, startTime, endTime);
        if (lots.findByItemId(itemId).stream().anyMatch(l -> l.status() == LotStatus.ACTIVE)) {
            throw ApiException.conflict("Предмет уже выставлен на активный лот");
        }
        return lots.save(new AuctionLot(lots.nextId(), itemId, startPrice, bidStep,
                startTime, endTime, LotStatus.ACTIVE));
    }

    public AuctionLot update(Long id, BigDecimal startPrice, BigDecimal bidStep,
                             Instant startTime, Instant endTime, LotStatus status) {
        AuctionLot old = getById(id);
        boolean hasBids = !bidRepo.findByLotId(id).isEmpty();
        if (hasBids && (startPrice != null || bidStep != null)) {
            throw ApiException.conflict("Нельзя менять цену и шаг у лота, на который уже сделаны ставки");
        }
        BigDecimal newPrice = startPrice != null ? startPrice : old.startPrice();
        BigDecimal newStep = bidStep != null ? bidStep : old.bidStep();
        Instant newStart = startTime != null ? startTime : old.startTime();
        Instant newEnd = endTime != null ? endTime : old.endTime();
        validateMoneyAndTime(newPrice, newStep, newStart, newEnd);
        return lots.save(new AuctionLot(id, old.itemId(), newPrice, newStep, newStart, newEnd,
                status != null ? status : old.status()));
    }

    public void delete(Long id) {
        getById(id);
        deals.deleteByLot(id);
        bids.deleteByLot(id);
        lots.deleteById(id);
    }

    private static void validateMoneyAndTime(BigDecimal price, BigDecimal step, Instant start, Instant end) {
        if (price.signum() <= 0) throw ApiException.badRequest("startPrice должна быть больше 0");
        if (step.signum() <= 0) throw ApiException.badRequest("bidStep должен быть больше 0");
        if (!end.isAfter(start)) throw ApiException.badRequest("endTime должен быть позже startTime");
    }
}
