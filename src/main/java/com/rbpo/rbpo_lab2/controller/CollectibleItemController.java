package com.rbpo.rbpo_lab2.controller;

import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import com.rbpo.rbpo_lab2.dto.CollectibleItemDto;
import com.rbpo.rbpo_lab2.dto.UserDto;
import com.rbpo.rbpo_lab2.model.CollectibleItem;
import com.rbpo.rbpo_lab2.model.User;
import com.rbpo.rbpo_lab2.security.CurrentUser;
import com.rbpo.rbpo_lab2.service.CollectibleItemService;
import com.rbpo.rbpo_lab2.service.UserService;

@RestController
@RequestMapping("/api/collectible-items")
public class CollectibleItemController {

    private final CollectibleItemService service;
    private final UserService userService;
    private final CurrentUser currentUser;

    public CollectibleItemController(CollectibleItemService service,
                                     UserService userService,
                                     CurrentUser currentUser) {
        this.service = service;
        this.userService = userService;
        this.currentUser = currentUser;
    }

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public List<CollectibleItemDto.Response> getAll() {
        return service.getAll().stream().map(this::toResponse).toList();
    }

    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public CollectibleItemDto.Response getById(@PathVariable Long id) {
        return toResponse(service.getById(id));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("isAuthenticated()")
    public CollectibleItemDto.Response create(@RequestBody CollectibleItemDto.CreateRequest req) {
        Long ownerId = currentUser.isAdmin() && req.ownerId() != null ? req.ownerId() : currentUser.id();
        return toResponse(service.create(req.name(), req.description(), ownerId, req.condition()));
    }

    @PatchMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or @authz.isItemOwner(#id, authentication)")
    public CollectibleItemDto.Response update(@PathVariable Long id,
                                              @RequestBody CollectibleItemDto.UpdateRequest req) {
        Long ownerId = currentUser.isAdmin() && req.ownerId() != null ? req.ownerId() : currentUser.id();
        return toResponse(service.update(id, req.name(), req.description(), ownerId, req.condition()));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasRole('ADMIN') or @authz.isItemOwner(#id, authentication)")
    public void delete(@PathVariable Long id) {
        service.delete(id);
    }

    private CollectibleItemDto.Response toResponse(CollectibleItem i) {
        User owner = userService.getById(i.ownerId());
        return new CollectibleItemDto.Response(i.id(), i.name(), i.description(),
                new UserDto.Response(owner.getId(), owner.getUsername(), owner.getEmail()), i.condition());
    }
}