package com.rbpo.rbpo_lab2.controller;

import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import com.rbpo.rbpo_lab2.dto.AuctionDealDto;
import com.rbpo.rbpo_lab2.model.AuctionDeal;
import com.rbpo.rbpo_lab2.service.AuctionDealService;

@RestController
@RequestMapping("/api/auction-deals")
public class AuctionDealController {
    private final AuctionDealService service;

    public AuctionDealController(AuctionDealService service) { this.service = service; }

    @GetMapping
    public List<AuctionDealDto.Response> getAll() {
        return service.getAll().stream().map(AuctionDealController::toResponse).toList();
    }

    @GetMapping("/{id}")
    public AuctionDealDto.Response getById(@PathVariable Long id) {
        return toResponse(service.getById(id));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public AuctionDealDto.Response create(@RequestBody AuctionDealDto.CreateRequest req) {
        return toResponse(service.create(req.lotId(), req.bidId(), req.transferStatus()));
    }

    @PatchMapping("/{id}")
    public AuctionDealDto.Response update(@PathVariable Long id, @RequestBody AuctionDealDto.UpdateRequest req) {
        return toResponse(service.update(id, req.transferStatus()));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) { service.delete(id); }

    private static AuctionDealDto.Response toResponse(AuctionDeal d) {
        return new AuctionDealDto.Response(d.id(), d.lotId(), d.bidId(), d.amount(), d.transferStatus());
    }
}
