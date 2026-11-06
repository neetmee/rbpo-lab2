package com.rbpo.rbpo_lab2.security;

import com.rbpo.rbpo_lab2.model.*;
import com.rbpo.rbpo_lab2.repository.*;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

@Component("authz")
public class AuthzChecks {

    private final CollectibleItemRepository itemRepo;
    private final AuctionLotRepository lotRepo;
    private final BidRepository bidRepo;
    private final AuctionDealRepository dealRepo;
    private final UserRepository userRepo;

    public AuthzChecks(CollectibleItemRepository itemRepo,
                       AuctionLotRepository lotRepo,
                       BidRepository bidRepo,
                       AuctionDealRepository dealRepo,
                       UserRepository userRepo) {
        this.itemRepo = itemRepo;
        this.lotRepo = lotRepo;
        this.bidRepo = bidRepo;
        this.dealRepo = dealRepo;
        this.userRepo = userRepo;
    }

    private Long getUserId(Authentication auth) {
        if (auth == null || !auth.isAuthenticated()) return null;
        return userRepo.findByUsername(auth.getName()).map(User::getId).orElse(null);
    }

    public boolean isItemOwner(Long itemId, Authentication auth) {
        if (itemId == null) return false;
        Long uid = getUserId(auth);
        return uid != null && itemRepo.findById(itemId)
                .map(i -> i.ownerId().equals(uid)).orElse(false);
    }

    public boolean isLotSeller(Long lotId, Authentication auth) {
        if (lotId == null) return false;
        Long uid = getUserId(auth);
        return uid != null && lotRepo.findById(lotId)
                .flatMap(lot -> itemRepo.findById(lot.itemId()))
                .map(item -> item.ownerId().equals(uid)).orElse(false);
    }

    public boolean isBidAuthor(Long bidId, Authentication auth) {
        if (bidId == null) return false;
        Long uid = getUserId(auth);
        return uid != null && bidRepo.findById(bidId)
                .map(b -> b.userId().equals(uid)).orElse(false);
    }

    public boolean isDealParticipant(Long dealId, Authentication auth) {
        if (dealId == null) return false;
        Long uid = getUserId(auth);
        if (uid == null) return false;
        return dealRepo.findById(dealId).map(deal -> {
            boolean isBuyer = bidRepo.findById(deal.bidId())
                    .map(b -> b.userId().equals(uid)).orElse(false);
            boolean isSeller = lotRepo.findById(deal.lotId())
                    .flatMap(lot -> itemRepo.findById(lot.itemId()))
                    .map(item -> item.ownerId().equals(uid)).orElse(false);
            return isBuyer || isSeller;
        }).orElse(false);
    }

    public boolean isDealSeller(Long dealId, Authentication auth) {
        if (dealId == null) return false;
        Long uid = getUserId(auth);
        return uid != null && dealRepo.findById(dealId)
                .flatMap(deal -> lotRepo.findById(deal.lotId()))
                .flatMap(lot -> itemRepo.findById(lot.itemId()))
                .map(item -> item.ownerId().equals(uid)).orElse(false);
    }
}