package com.rbpo.rbpo_lab2.service;

import com.rbpo.rbpo_lab2.exception.ApiException;
import com.rbpo.rbpo_lab2.model.AuctionLot;
import com.rbpo.rbpo_lab2.model.CollectibleItem;
import com.rbpo.rbpo_lab2.model.LotStatus;
import com.rbpo.rbpo_lab2.repository.AuctionLotRepository;
import com.rbpo.rbpo_lab2.repository.BidRepository;
import com.rbpo.rbpo_lab2.repository.CollectibleItemRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

@Service
@Transactional
public class AuctionLotService {

    private final AuctionLotRepository lots;
    private final CollectibleItemRepository items;
    private final BidRepository bidRepo;
    private final BidService bids;
    private final AuctionDealService deals;

    public AuctionLotService(AuctionLotRepository lots,
                             CollectibleItemRepository items,
                             BidRepository bidRepo,
                             BidService bids,
                             AuctionDealService deals) {
        this.lots = lots;
        this.items = items;
        this.bidRepo = bidRepo;
        this.bids = bids;
        this.deals = deals;
    }

    @Transactional(readOnly = true)
    public List<AuctionLot> getAll() {
        return lots.findAll();
    }

    @Transactional(readOnly = true)
    public AuctionLot getById(Long id) {
        return lots.findById(id)
                .orElseThrow(() -> ApiException.notFound("Лот", id));
    }

    public AuctionLot create(Long itemId, BigDecimal startPrice, BigDecimal bidStep,
                             Instant startTime, Instant endTime) {
        if (itemId == null || startPrice == null || bidStep == null || startTime == null || endTime == null) {
            throw ApiException.badRequest("Поля itemId, startPrice, bidStep, startTime, endTime обязательны");
        }

        CollectibleItem item = items.findById(itemId)
                .orElseThrow(() -> ApiException.badRequest("Предмет с id=" + itemId + " не существует"));

        validateMoneyAndTime(startPrice, bidStep, startTime, endTime);

        if (lots.existsByItemIdAndStatusIn(itemId, List.of(LotStatus.CREATED, LotStatus.ACTIVE))) {
            throw ApiException.conflict("Предмет уже выставлен на активный лот");
        }

        return lots.save(new AuctionLot(item, startPrice, bidStep, startTime, endTime, LotStatus.CREATED));
    }

    public AuctionLot update(Long id, BigDecimal startPrice, BigDecimal bidStep,
                             Instant startTime, Instant endTime, LotStatus status) {
        AuctionLot lot = getById(id);

        boolean hasBids = bidRepo.existsByLotId(id);
        if (hasBids && (startPrice != null || bidStep != null)) {
            throw ApiException.conflict("Нельзя менять цену и шаг у лота, на который уже сделаны ставки");
        }

        BigDecimal newPrice = startPrice != null ? startPrice : lot.getStartPrice();
        BigDecimal newStep = bidStep != null ? bidStep : lot.getBidStep();
        Instant newStart = startTime != null ? startTime : lot.getStartTime();
        Instant newEnd = endTime != null ? endTime : lot.getEndTime();
        validateMoneyAndTime(newPrice, newStep, newStart, newEnd);

        lot.setStartPrice(newPrice);
        lot.setBidStep(newStep);
        lot.setStartTime(newStart);
        lot.setEndTime(newEnd);
        if (status != null) {
            lot.setStatus(status);
        }

        return lots.save(lot);
    }

    public void delete(Long id) {
        AuctionLot lot = getById(id);
        deals.deleteByLot(id);
        bids.deleteByLot(id);
        lots.delete(lot);
    }

    public void deleteByItem(Long itemId) {
        lots.findAllByItemId(itemId).forEach(lot -> delete(lot.getId()));
    }

    private static void validateMoneyAndTime(BigDecimal price, BigDecimal step, Instant start, Instant end) {
        if (price.signum() <= 0) {
            throw ApiException.badRequest("startPrice должна быть больше 0");
        }
        if (step.signum() <= 0) {
            throw ApiException.badRequest("bidStep должен быть больше 0");
        }
        if (!end.isAfter(start)) {
            throw ApiException.badRequest("endTime должен быть позже startTime");
        }
    }
}