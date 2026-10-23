package com.rbpo.rbpo_lab2.controller;

import com.rbpo.rbpo_lab2.model.AuctionDeal;
import com.rbpo.rbpo_lab2.model.AuctionLot;
import com.rbpo.rbpo_lab2.model.Bid;
import com.rbpo.rbpo_lab2.service.AuctionBusinessService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.Map;

@RestController
@RequestMapping("/api/business")
public class AuctionBusinessController {

    private final AuctionBusinessService businessService;

    public AuctionBusinessController(AuctionBusinessService businessService) {
        this.businessService = businessService;
    }

    @PostMapping("/lots/{lotId}/activate")
    public ResponseEntity<AuctionLot> activateLot(@PathVariable Long lotId) {
        AuctionLot lot = businessService.activateLot(lotId);
        return ResponseEntity.ok(lot);
    }

    @PostMapping("/lots/{lotId}/bids")
    public ResponseEntity<Bid> placeBid(@PathVariable Long lotId, @RequestBody Map<String, Object> payload) {
        Long userId = Long.valueOf(payload.get("userId").toString());
        BigDecimal amount = new BigDecimal(payload.get("amount").toString());
        Bid bid = businessService.placeCompetitiveBid(lotId, userId, amount);
        return ResponseEntity.ok(bid);
    }

    @PostMapping("/lots/{lotId}/close")
    public ResponseEntity<AuctionDeal> closeAuction(@PathVariable Long lotId) {
        AuctionDeal deal = businessService.closeAuctionLot(lotId);
        return ResponseEntity.ok(deal);
    }

    @PostMapping("/deals/{dealId}/complete")
    public ResponseEntity<AuctionDeal> completeDeal(@PathVariable Long dealId) {
        AuctionDeal deal = businessService.completeDealAndTransferOwnership(dealId);
        return ResponseEntity.ok(deal);
    }

    @PostMapping("/lots/{lotId}/cancel")
    public ResponseEntity<AuctionLot> cancelLot(@PathVariable Long lotId) {
        AuctionLot lot = businessService.cancelLot(lotId);
        return ResponseEntity.ok(lot);
    }
}