package com.rbpo.rbpo_lab2.controller;

import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import com.rbpo.rbpo_lab2.dto.AuctionLotDto;
import com.rbpo.rbpo_lab2.model.AuctionLot;
import com.rbpo.rbpo_lab2.service.AuctionLotService;

@RestController
@RequestMapping("/api/auction-lots")
public class AuctionLotController {
    private final AuctionLotService service;

    public AuctionLotController(AuctionLotService service) { this.service = service; }

    @GetMapping
    public List<AuctionLotDto.Response> getAll() {
        return service.getAll().stream().map(AuctionLotController::toResponse).toList();
    }

    @GetMapping("/{id}")
    public AuctionLotDto.Response getById(@PathVariable Long id) {
        return toResponse(service.getById(id));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public AuctionLotDto.Response create(@RequestBody AuctionLotDto.CreateRequest req) {
        return toResponse(service.create(req.itemId(), req.startPrice(), req.bidStep(),
                req.startTime(), req.endTime()));
    }

    @PatchMapping("/{id}")
    public AuctionLotDto.Response update(@PathVariable Long id, @RequestBody AuctionLotDto.UpdateRequest req) {
        return toResponse(service.update(id, req.startPrice(), req.bidStep(),
                req.startTime(), req.endTime(), req.status()));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) { service.delete(id); }

    private static AuctionLotDto.Response toResponse(AuctionLot l) {
        return new AuctionLotDto.Response(l.id(), l.itemId(), l.startPrice(), l.bidStep(),
                l.startTime(), l.endTime(), l.status());
    }
}
