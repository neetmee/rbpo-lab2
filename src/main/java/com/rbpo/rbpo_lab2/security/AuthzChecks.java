package com.rbpo.rbpo_lab2.security;

import com.rbpo.rbpo_lab2.model.User;
import com.rbpo.rbpo_lab2.repository.*;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

@Component("authz")
public class AuthzChecks {

    private final UserRepository userRepository;
    private final CollectibleItemRepository itemRepository;
    private final AuctionLotRepository lotRepository;
    private final BidRepository bidRepository;
    private final AuctionDealRepository dealRepository;

    public AuthzChecks(UserRepository userRepository,
                       CollectibleItemRepository itemRepository,
                       AuctionLotRepository lotRepository,
                       BidRepository bidRepository,
                       AuctionDealRepository dealRepository) {
        this.userRepository = userRepository;
        this.itemRepository = itemRepository;
        this.lotRepository = lotRepository;
        this.bidRepository = bidRepository;
        this.dealRepository = dealRepository;
    }

    private Long getAuthUserId(Authentication auth) {
        if (auth == null) return null;
        return userRepository.findByUsername(auth.getName()).map(User::getId).orElse(null);
    }

    public boolean isItemOwner(Long itemId, Authentication auth) {
        Long currentUserId = getAuthUserId(auth);
        return itemRepository.findById(itemId)
                .map(item -> item.ownerId().equals(currentUserId))
                .orElse(false);
    }

    public boolean isLotSeller(Long lotId, Authentication auth) {
        Long currentUserId = getAuthUserId(auth);
        return lotRepository.findById(lotId)
                .flatMap(lot -> itemRepository.findById(lot.itemId()))
                .map(item -> item.ownerId().equals(currentUserId))
                .orElse(false);
    }

    public boolean isBidAuthor(Long bidId, Authentication auth) {
        Long currentUserId = getAuthUserId(auth);
        return bidRepository.findById(bidId)
                .map(bid -> bid.userId().equals(currentUserId))
                .orElse(false);
    }

    public boolean isDealSeller(Long dealId, Authentication auth) {
        Long currentUserId = getAuthUserId(auth);
        return dealRepository.findById(dealId)
                .flatMap(deal -> lotRepository.findById(deal.lotId()))
                .flatMap(lot -> itemRepository.findById(lot.itemId()))
                .map(item -> item.ownerId().equals(currentUserId))
                .orElse(false);
    }

    public boolean isDealParticipant(Long dealId, Authentication auth) {
        Long currentUserId = getAuthUserId(auth);
        return dealRepository.findById(dealId).map(deal -> {
            boolean isWinner = bidRepository.findById(deal.bidId())
                    .map(bid -> bid.userId().equals(currentUserId))
                    .orElse(false);
            return isWinner || isDealSeller(dealId, auth);
        }).orElse(false);
    }
}