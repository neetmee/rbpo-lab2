package com.rbpo.rbpo_lab2.controller;

import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import com.rbpo.rbpo_lab2.dto.UserDto;
import com.rbpo.rbpo_lab2.model.User;
import com.rbpo.rbpo_lab2.service.UserService;

@RestController
@RequestMapping("/api/users")
public class UserController {
    private final UserService service;

    public UserController(UserService service) { this.service = service; }

    @GetMapping
    public List<UserDto.Response> getAll() {
        return service.getAll().stream().map(UserController::toResponse).toList();
    }

    @GetMapping("/{id}")
    public UserDto.Response getById(@PathVariable Long id) {
        return toResponse(service.getById(id));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public UserDto.Response create(@RequestBody UserDto.CreateRequest req) {
        return toResponse(service.create(req.username(), req.email()));
    }

    @PatchMapping("/{id}")
    public UserDto.Response update(@PathVariable Long id, @RequestBody UserDto.UpdateRequest req) {
        return toResponse(service.update(id, req.username(), req.email()));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) { service.delete(id); }

    private static UserDto.Response toResponse(User u) {
        return new UserDto.Response(u.id(), u.username(), u.email());
    }
}
