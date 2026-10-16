package com.rbpo.rbpo_lab2.service;

import java.math.BigDecimal;
import java.util.List;
import org.springframework.stereotype.Service;
import com.rbpo.rbpo_lab2.exception.ApiException;
import com.rbpo.rbpo_lab2.model.AuctionDeal;
import com.rbpo.rbpo_lab2.model.AuctionLot;
import com.rbpo.rbpo_lab2.model.Bid;
import com.rbpo.rbpo_lab2.model.LotStatus;
import com.rbpo.rbpo_lab2.model.TransferStatus;
import com.rbpo.rbpo_lab2.repository.AuctionDealRepository;
import com.rbpo.rbpo_lab2.repository.AuctionLotRepository;
import com.rbpo.rbpo_lab2.repository.BidRepository;

@Service
public class AuctionDealService {
    private final AuctionDealRepository deals;
    private final AuctionLotRepository lots;
    private final BidRepository bids;

    public AuctionDealService(AuctionDealRepository deals, AuctionLotRepository lots, BidRepository bids) {
        this.deals = deals;
        this.lots = lots;
        this.bids = bids;
    }

    public List<AuctionDeal> getAll() { return deals.findAll(); }

    public AuctionDeal getById(Long id) {
        return deals.findById(id).orElseThrow(() -> ApiException.notFound("Сделка", id));
    }

    public List<AuctionDeal> findByBid(Long bidId) { return deals.findByBidId(bidId); }

    public AuctionDeal create(Long lotId, Long bidId, TransferStatus transferStatus) {
        if (lotId == null || bidId == null) throw ApiException.badRequest("Поля lotId и bidId обязательны");
        AuctionLot lot = lots.findById(lotId)
                .orElseThrow(() -> ApiException.badRequest("Лот с id=" + lotId + " не существует"));
        Bid bid = bids.findById(bidId)
                .orElseThrow(() -> ApiException.badRequest("Ставка с id=" + bidId + " не существует"));
        if (!bid.lotId().equals(lotId)) throw ApiException.badRequest("Ставка относится к другому лоту");
        if (!deals.findByLotId(lotId).isEmpty()) throw ApiException.conflict("По лоту уже есть сделка");

        BigDecimal max = bids.findByLotId(lotId).stream().map(Bid::amount)
                .max(BigDecimal::compareTo).orElseThrow();
        if (bid.amount().compareTo(max) < 0) {
            throw ApiException.badRequest("Выигрышной может быть только максимальная ставка");
        }
        // сумма берётся из ставки, а не из запроса
        AuctionDeal deal = deals.save(new AuctionDeal(deals.nextId(), lotId, bidId, bid.amount(),
                transferStatus != null ? transferStatus : TransferStatus.PENDING));
        lots.save(new AuctionLot(lot.id(), lot.itemId(), lot.startPrice(), lot.bidStep(),
                lot.startTime(), lot.endTime(), LotStatus.CLOSED));
        return deal;
    }

    public AuctionDeal update(Long id, TransferStatus transferStatus) {
        AuctionDeal old = getById(id);
        if (transferStatus == null) return old;
        return deals.save(new AuctionDeal(id, old.lotId(), old.bidId(), old.amount(), transferStatus));
    }

    public void delete(Long id) {
        getById(id);
        deals.deleteById(id);
    }

    public void deleteByLot(Long lotId) {
        deals.findByLotId(lotId).forEach(d -> deals.deleteById(d.id()));
    }

    public void deleteByBid(Long bidId) {
        deals.findByBidId(bidId).forEach(d -> deals.deleteById(d.id()));
    }
}
