package com.rbpo.rbpo_lab2.controller;

import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import com.rbpo.rbpo_lab2.dto.CollectibleItemDto;
import com.rbpo.rbpo_lab2.dto.UserDto;
import com.rbpo.rbpo_lab2.model.CollectibleItem;
import com.rbpo.rbpo_lab2.model.User;
import com.rbpo.rbpo_lab2.service.CollectibleItemService;
import com.rbpo.rbpo_lab2.service.UserService;

@RestController
@RequestMapping("/api/collectible-items")
public class CollectibleItemController {
    private final CollectibleItemService service;
    private final UserService userService;

    public CollectibleItemController(CollectibleItemService service, UserService userService) {
        this.service = service;
        this.userService = userService;
    }

    @GetMapping
    public List<CollectibleItemDto.Response> getAll() {
        return service.getAll().stream().map(this::toResponse).toList();
    }

    @GetMapping("/{id}")
    public CollectibleItemDto.Response getById(@PathVariable Long id) {
        return toResponse(service.getById(id));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CollectibleItemDto.Response create(@RequestBody CollectibleItemDto.CreateRequest req) {
        return toResponse(service.create(req.name(), req.description(), req.ownerId(), req.condition()));
    }

    @PatchMapping("/{id}")
    public CollectibleItemDto.Response update(@PathVariable Long id,
                                              @RequestBody CollectibleItemDto.UpdateRequest req) {
        return toResponse(service.update(id, req.name(), req.description(), req.ownerId(), req.condition()));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) { service.delete(id); }

    private CollectibleItemDto.Response toResponse(CollectibleItem i) {
        User owner = userService.getById(i.ownerId());
        return new CollectibleItemDto.Response(i.id(), i.name(), i.description(),
                new UserDto.Response(owner.id(), owner.username(), owner.email()), i.condition());
    }
}
