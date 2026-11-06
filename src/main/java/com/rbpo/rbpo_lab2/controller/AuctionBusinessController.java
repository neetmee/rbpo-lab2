package com.rbpo.rbpo_lab2.controller;

import com.rbpo.rbpo_lab2.dto.*;
import com.rbpo.rbpo_lab2.model.*;
import com.rbpo.rbpo_lab2.security.CurrentUser;
import com.rbpo.rbpo_lab2.service.AuctionBusinessService;
import com.rbpo.rbpo_lab2.service.UserService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

@RestController
@RequestMapping("/api/business")
public class AuctionBusinessController {

    private final AuctionBusinessService businessService;
    private final UserService userService;
    private final CurrentUser currentUser;

    public AuctionBusinessController(AuctionBusinessService businessService,
                                     UserService userService,
                                     CurrentUser currentUser) {
        this.businessService = businessService;
        this.userService = userService;
        this.currentUser = currentUser;
    }

    public record PlaceBidRequest(@NotNull @Positive BigDecimal amount) {}

    @PostMapping("/lots/{lotId}/activate")
    @PreAuthorize("hasRole('ADMIN') or @authz.isLotSeller(#lotId, authentication)")
    public ResponseEntity<AuctionLotDto.Response> activateLot(@PathVariable Long lotId) {
        AuctionLot lot = businessService.activateLot(lotId);
        return ResponseEntity.ok(toLotResponse(lot));
    }

    @PostMapping("/lots/{lotId}/bids")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<BidDto.Response> placeBid(@PathVariable Long lotId,
                                                    @Valid @RequestBody PlaceBidRequest payload) { // @Valid добавлен
        Bid bid = businessService.placeCompetitiveBid(lotId, currentUser.id(), payload.amount());
        return ResponseEntity.ok(toBidResponse(bid));
    }

    @PostMapping("/lots/{lotId}/close")
    @PreAuthorize("hasRole('ADMIN') or @authz.isLotSeller(#lotId, authentication)")
    public ResponseEntity<AuctionDealDto.Response> closeAuction(@PathVariable Long lotId) {
        AuctionDeal deal = businessService.closeAuctionLot(lotId);
        // Без ставок возвращаем 204 No Content вместо 500 NPE
        return deal == null ? ResponseEntity.noContent().build() : ResponseEntity.ok(toDealResponse(deal));
    }

    @PostMapping("/deals/{dealId}/complete")
    @PreAuthorize("hasRole('ADMIN') or @authz.isDealSeller(#dealId, authentication)")
    public ResponseEntity<AuctionDealDto.Response> completeDeal(@PathVariable Long dealId) {
        AuctionDeal deal = businessService.completeDealAndTransferOwnership(dealId);
        return ResponseEntity.ok(toDealResponse(deal));
    }

    @PostMapping("/lots/{lotId}/cancel")
    @PreAuthorize("hasRole('ADMIN') or @authz.isLotSeller(#lotId, authentication)")
    public ResponseEntity<AuctionLotDto.Response> cancelLot(@PathVariable Long lotId) {
        AuctionLot lot = businessService.cancelLot(lotId);
        return ResponseEntity.ok(toLotResponse(lot));
    }

    private AuctionLotDto.Response toLotResponse(AuctionLot l) {
        return new AuctionLotDto.Response(l.id(), l.itemId(), l.startPrice(), l.bidStep(), l.startTime(), l.endTime(), l.status());
    }

    private AuctionDealDto.Response toDealResponse(AuctionDeal d) {
        return new AuctionDealDto.Response(d.id(), d.lotId(), d.bidId(), d.amount(), d.transferStatus());
    }

    private BidDto.Response toBidResponse(Bid b) {
        User u = userService.getById(b.userId());
        return new BidDto.Response(b.id(), b.lotId(), new UserDto.Public(u.getId(), u.getUsername()), b.amount(), b.createdAt());
    }
}