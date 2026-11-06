package com.rbpo.rbpo_lab2.controller;

import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import com.rbpo.rbpo_lab2.dto.UserDto;
import com.rbpo.rbpo_lab2.model.User;
import com.rbpo.rbpo_lab2.service.UserService;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService service;

    public UserController(UserService service) {
        this.service = service;
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public List<UserDto.Response> getAll() {
        return service.getAll().stream().map(UserController::toResponse).toList();
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or #id == @currentUser.id()")
    public UserDto.Response getById(@PathVariable Long id) {
        return toResponse(service.getById(id));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasRole('ADMIN')")
    public UserDto.Response create(@RequestBody UserDto.CreateRequest req) {
        return toResponse(service.create(req.username(), req.email()));
    }

    @PatchMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or #id == @currentUser.id()")
    public UserDto.Response update(@PathVariable Long id, @RequestBody UserDto.UpdateRequest req) {
        return toResponse(service.update(id, req.username(), req.email()));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasRole('ADMIN')")
    public void delete(@PathVariable Long id) {
        service.delete(id);
    }

    private static UserDto.Response toResponse(User u) {
        return new UserDto.Response(u.getId(), u.getUsername(), u.getEmail());
    }
}