package com.rbpo.rbpo_lab2.controller;

import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import com.rbpo.rbpo_lab2.dto.AuctionLotDto;
import com.rbpo.rbpo_lab2.model.AuctionLot;
import com.rbpo.rbpo_lab2.security.CurrentUser;
import com.rbpo.rbpo_lab2.service.AuctionLotService;

@RestController
@RequestMapping("/api/auction-lots")
public class AuctionLotController {

    private final AuctionLotService service;
    private final CurrentUser currentUser;

    public AuctionLotController(AuctionLotService service, CurrentUser currentUser) {
        this.service = service;
        this.currentUser = currentUser;
    }

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public List<AuctionLotDto.Response> getAll() {
        return service.getAll().stream().map(AuctionLotController::toResponse).toList();
    }

    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public AuctionLotDto.Response getById(@PathVariable Long id) {
        return toResponse(service.getById(id));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasRole('ADMIN') or @authz.isItemOwner(#req.itemId(), authentication)")
    public AuctionLotDto.Response create(@RequestBody AuctionLotDto.CreateRequest req) {
        return toResponse(service.create(req.itemId(), req.startPrice(), req.bidStep(),
                req.startTime(), req.endTime()));
    }

    @PatchMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or @authz.isLotSeller(#id, authentication)")
    public AuctionLotDto.Response update(@PathVariable Long id, @RequestBody AuctionLotDto.UpdateRequest req) {
        if (req.status() != null && !currentUser.isAdmin()) {
            throw new AccessDeniedException("Изменять статус лота вручную разрешено только администратору");
        }
        return toResponse(service.update(id, req.startPrice(), req.bidStep(),
                req.startTime(), req.endTime(), req.status()));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasRole('ADMIN') or @authz.isLotSeller(#id, authentication)")
    public void delete(@PathVariable Long id) {
        service.delete(id);
    }

    private static AuctionLotDto.Response toResponse(AuctionLot l) {
        return new AuctionLotDto.Response(l.id(), l.itemId(), l.startPrice(), l.bidStep(),
                l.startTime(), l.endTime(), l.status());
    }
}