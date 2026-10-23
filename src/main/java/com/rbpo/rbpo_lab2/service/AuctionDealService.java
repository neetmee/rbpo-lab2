package com.rbpo.rbpo_lab2.service;

import com.rbpo.rbpo_lab2.exception.ApiException;
import com.rbpo.rbpo_lab2.model.AuctionDeal;
import com.rbpo.rbpo_lab2.model.AuctionLot;
import com.rbpo.rbpo_lab2.model.Bid;
import com.rbpo.rbpo_lab2.model.LotStatus;
import com.rbpo.rbpo_lab2.model.TransferStatus;
import com.rbpo.rbpo_lab2.repository.AuctionDealRepository;
import com.rbpo.rbpo_lab2.repository.AuctionLotRepository;
import com.rbpo.rbpo_lab2.repository.BidRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
@Transactional
public class AuctionDealService {

    private final AuctionDealRepository deals;
    private final AuctionLotRepository lots;
    private final BidRepository bids;

    public AuctionDealService(AuctionDealRepository deals, AuctionLotRepository lots, BidRepository bids) {
        this.deals = deals;
        this.lots = lots;
        this.bids = bids;
    }

    @Transactional(readOnly = true)
    public List<AuctionDeal> getAll() {
        return deals.findAll();
    }

    @Transactional(readOnly = true)
    public AuctionDeal getById(Long id) {
        return deals.findById(id)
                .orElseThrow(() -> ApiException.notFound("Сделка", id));
    }

    @Transactional(readOnly = true)
    public List<AuctionDeal> findByBid(Long bidId) {
        return deals.findAllByBidId(bidId);
    }

    public AuctionDeal create(Long lotId, Long bidId, TransferStatus transferStatus) {
        if (lotId == null || bidId == null) {
            throw ApiException.badRequest("Поля lotId и bidId обязательны");
        }

        AuctionLot lot = lots.findById(lotId)
                .orElseThrow(() -> ApiException.badRequest("Лот с id=" + lotId + " не существует"));

        Bid bid = bids.findById(bidId)
                .orElseThrow(() -> ApiException.badRequest("Ставка с id=" + bidId + " не существует"));

        if (!bid.getLot().getId().equals(lotId)) {
            throw ApiException.badRequest("Ставка относится к другому лоту");
        }

        if (deals.existsByLotId(lotId)) {
            throw ApiException.conflict("По лоту уже есть сделка");
        }

        BigDecimal max = bids.findFirstByLotIdOrderByAmountDesc(lotId)
                .orElseThrow(() -> ApiException.badRequest("У лота отсутствуют ставки"))
                .getAmount();

        if (bid.getAmount().compareTo(max) < 0) {
            throw ApiException.badRequest("Выигрышной может быть только максимальная ставка");
        }

        // Сумма фиксируется из ставки, дефолтный статус — PENDING
        TransferStatus status = transferStatus != null ? transferStatus : TransferStatus.PENDING;
        AuctionDeal deal = deals.save(new AuctionDeal(lot, bid, bid.getAmount(), status));

        lot.setStatus(LotStatus.CLOSED);
        lots.save(lot);

        return deal;
    }

    public AuctionDeal update(Long id, TransferStatus transferStatus) {
        AuctionDeal deal = getById(id);
        if (transferStatus == null) {
            return deal;
        }
        deal.setTransferStatus(transferStatus);
        return deals.save(deal);
    }

    public void delete(Long id) {
        deals.delete(getById(id));
    }

    public void deleteByLot(Long lotId) {
        deals.deleteAll(deals.findAllByLotId(lotId));
    }

    public void deleteByBid(Long bidId) {
        deals.deleteAll(deals.findAllByBidId(bidId));
    }
}