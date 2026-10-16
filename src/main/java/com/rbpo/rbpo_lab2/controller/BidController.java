package com.rbpo.rbpo_lab2.controller;

import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import com.rbpo.rbpo_lab2.dto.BidDto;
import com.rbpo.rbpo_lab2.dto.UserDto;
import com.rbpo.rbpo_lab2.model.Bid;
import com.rbpo.rbpo_lab2.model.User;
import com.rbpo.rbpo_lab2.service.BidService;
import com.rbpo.rbpo_lab2.service.UserService;

@RestController
@RequestMapping("/api/bids")
public class BidController {
    private final BidService service;
    private final UserService userService;

    public BidController(BidService service, UserService userService) {
        this.service = service;
        this.userService = userService;
    }

    @GetMapping
    public List<BidDto.Response> getAll() {
        return service.getAll().stream().map(this::toResponse).toList();
    }

    @GetMapping("/{id}")
    public BidDto.Response getById(@PathVariable Long id) {
        return toResponse(service.getById(id));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public BidDto.Response create(@RequestBody BidDto.CreateRequest req) {
        return toResponse(service.create(req.lotId(), req.userId(), req.amount()));
    }

    @PatchMapping("/{id}")
    public BidDto.Response update(@PathVariable Long id, @RequestBody BidDto.UpdateRequest req) {
        return toResponse(service.update(id, req.amount()));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) { service.delete(id); }

    private BidDto.Response toResponse(Bid b) {
        User u = userService.getById(b.userId());
        return new BidDto.Response(b.id(), b.lotId(),
                new UserDto.Response(u.id(), u.username(), u.email()), b.amount(), b.createdAt());
    }
}
