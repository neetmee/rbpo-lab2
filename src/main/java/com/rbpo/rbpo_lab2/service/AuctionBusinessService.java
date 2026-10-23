package com.rbpo.rbpo_lab2.service;

import com.rbpo.rbpo_lab2.exception.ApiException;
import com.rbpo.rbpo_lab2.model.AuctionDeal;
import com.rbpo.rbpo_lab2.model.AuctionLot;
import com.rbpo.rbpo_lab2.model.Bid;
import com.rbpo.rbpo_lab2.model.CollectibleItem;
import com.rbpo.rbpo_lab2.model.LotStatus;
import com.rbpo.rbpo_lab2.model.TransferStatus;
import com.rbpo.rbpo_lab2.model.User;
import com.rbpo.rbpo_lab2.repository.AuctionDealRepository;
import com.rbpo.rbpo_lab2.repository.AuctionLotRepository;
import com.rbpo.rbpo_lab2.repository.BidRepository;
import com.rbpo.rbpo_lab2.repository.CollectibleItemRepository;
import com.rbpo.rbpo_lab2.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

@Service
@Transactional
public class AuctionBusinessService {

    private final AuctionLotRepository lotRepository;
    private final CollectibleItemRepository itemRepository;
    private final UserRepository userRepository;
    private final BidRepository bidRepository;
    private final AuctionDealRepository dealRepository;

    public AuctionBusinessService(AuctionLotRepository lotRepository,
                                  CollectibleItemRepository itemRepository,
                                  UserRepository userRepository,
                                  BidRepository bidRepository,
                                  AuctionDealRepository dealRepository) {
        this.lotRepository = lotRepository;
        this.itemRepository = itemRepository;
        this.userRepository = userRepository;
        this.bidRepository = bidRepository;
        this.dealRepository = dealRepository;
    }

    public AuctionLot activateLot(Long lotId) {
        AuctionLot lot = lotRepository.findById(lotId)
                .orElseThrow(() -> ApiException.notFound("Лот", lotId));

        if (lot.getStatus() != LotStatus.CREATED) {
            throw ApiException.conflict("Активировать можно только лот в статусе CREATED. Текущий статус: " + lot.getStatus());
        }

        Instant now = Instant.now();
        if (lot.getEndTime().isBefore(now)) {
            throw ApiException.badRequest("Нельзя активировать лот с истекшим сроком окончания");
        }

        boolean itemAlreadyInActiveAuction = lotRepository.existsByItemIdAndStatusIn(
                lot.getItem().getId(), List.of(LotStatus.ACTIVE)
        );
        if (itemAlreadyInActiveAuction) {
            throw ApiException.conflict("Предмет уже участвует в других активных торгах");
        }

        lot.setStatus(LotStatus.ACTIVE);
        return lotRepository.save(lot);
    }

    public Bid placeCompetitiveBid(Long lotId, Long userId, BigDecimal amount) {
        AuctionLot lot = lotRepository.findById(lotId)
                .orElseThrow(() -> ApiException.notFound("Лот", lotId));

        if (lot.getStatus() != LotStatus.ACTIVE) {
            throw ApiException.conflict("Ставки принимаются только на активные лоты");
        }

        Instant now = Instant.now();
        if (now.isBefore(lot.getStartTime()) || now.isAfter(lot.getEndTime())) {
            throw ApiException.badRequest("Аукцион не проводится в текущее время");
        }

        User bidder = userRepository.findById(userId)
                .orElseThrow(() -> ApiException.notFound("Пользователь", userId));

        if (lot.getItem().getOwner().getId().equals(bidder.getId())) {
            throw ApiException.conflict("Владелец предмета не может делать ставки на свой лот");
        }

        List<Bid> bids = bidRepository.findAllByLotIdOrderByAmountDesc(lotId);
        BigDecimal minAcceptableAmount;

        if (bids.isEmpty()) {
            minAcceptableAmount = lot.getStartPrice();
        } else {
            Bid highestBid = bids.get(0);
            if (highestBid.getUser().getId().equals(bidder.getId())) {
                throw ApiException.conflict("Ваша ставка уже является наивысшей");
            }
            minAcceptableAmount = highestBid.getAmount().add(lot.getBidStep());
        }

        if (amount.compareTo(minAcceptableAmount) < 0) {
            throw ApiException.badRequest("Размер ставки (" + amount + ") меньше минимальной: " + minAcceptableAmount);
        }

        Bid newBid = new Bid(lot, bidder, amount, now);
        return bidRepository.save(newBid);
    }

    public AuctionDeal closeAuctionLot(Long lotId) {
        AuctionLot lot = lotRepository.findById(lotId)
                .orElseThrow(() -> ApiException.notFound("Лот", lotId));

        if (lot.getStatus() != LotStatus.ACTIVE) {
            throw ApiException.conflict("Закрыть можно только лот в статусе ACTIVE");
        }

        lot.setStatus(LotStatus.CLOSED);
        lotRepository.save(lot);

        Bid winningBid = bidRepository.findFirstByLotIdOrderByAmountDesc(lotId).orElse(null);
        if (winningBid == null) {
            return null;
        }

        if (dealRepository.existsByLotId(lotId)) {
            throw ApiException.conflict("Сделка по данному лоту уже существует");
        }

        AuctionDeal deal = new AuctionDeal(lot, winningBid, winningBid.getAmount(), TransferStatus.PENDING);
        return dealRepository.save(deal);
    }

    public AuctionDeal completeDealAndTransferOwnership(Long dealId) {
        AuctionDeal deal = dealRepository.findById(dealId)
                .orElseThrow(() -> ApiException.notFound("Сделка", dealId));

        if (deal.getTransferStatus() != TransferStatus.PENDING) {
            throw ApiException.conflict("Завершить можно только сделку в статусе PENDING");
        }

        CollectibleItem item = deal.getLot().getItem();
        User winner = deal.getBid().getUser();

        item.setOwner(winner);
        itemRepository.save(item);

        deal.setTransferStatus(TransferStatus.TRANSFERRED);
        return dealRepository.save(deal);
    }

    public AuctionLot cancelLot(Long lotId) {
        AuctionLot lot = lotRepository.findById(lotId)
                .orElseThrow(() -> ApiException.notFound("Лот", lotId));

        if (lot.getStatus() == LotStatus.CANCELLED) {
            throw ApiException.conflict("Лот уже отменен");
        }

        dealRepository.findByLotId(lotId).ifPresent(deal -> {
            if (deal.getTransferStatus() == TransferStatus.TRANSFERRED) {
                throw ApiException.conflict("Нельзя отменить лот: права на предмет уже переданы покупателю");
            }
            deal.setTransferStatus(TransferStatus.CANCELLED);
            dealRepository.save(deal);
        });

        lot.setStatus(LotStatus.CANCELLED);
        return lotRepository.save(lot);
    }
}
