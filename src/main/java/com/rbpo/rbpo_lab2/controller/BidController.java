package com.rbpo.rbpo_lab2.controller;

import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import com.rbpo.rbpo_lab2.dto.BidDto;
import com.rbpo.rbpo_lab2.dto.UserDto;
import com.rbpo.rbpo_lab2.model.Bid;
import com.rbpo.rbpo_lab2.model.User;
import com.rbpo.rbpo_lab2.security.CurrentUser;
import com.rbpo.rbpo_lab2.service.BidService;
import com.rbpo.rbpo_lab2.service.UserService;

@RestController
@RequestMapping("/api/bids")
public class BidController {

    private final BidService service;
    private final UserService userService;
    private final CurrentUser currentUser;

    public BidController(BidService service, UserService userService, CurrentUser currentUser) {
        this.service = service;
        this.userService = userService;
        this.currentUser = currentUser;
    }

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public List<BidDto.Response> getAll() {
        return service.getAll().stream().map(this::toResponse).toList();
    }

    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public BidDto.Response getById(@PathVariable Long id) {
        return toResponse(service.getById(id));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("isAuthenticated()")
    public BidDto.Response create(@RequestBody BidDto.CreateRequest req) {
        Long bidderId = currentUser.isAdmin() && req.userId() != null ? req.userId() : currentUser.id();
        return toResponse(service.create(req.lotId(), bidderId, req.amount()));
    }

    @PatchMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or @authz.isBidAuthor(#id, authentication)")
    public BidDto.Response update(@PathVariable Long id, @RequestBody BidDto.UpdateRequest req) {
        return toResponse(service.update(id, req.amount()));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasRole('ADMIN') or @authz.isBidAuthor(#id, authentication)")
    public void delete(@PathVariable Long id) {
        service.delete(id);
    }

    private BidDto.Response toResponse(Bid b) {
        User u = userService.getById(b.userId());
        return new BidDto.Response(b.id(), b.lotId(),
                new UserDto.Response(u.getId(), u.getUsername(), u.getEmail()), b.amount(), b.createdAt());
    }
}