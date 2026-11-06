package com.rbpo.rbpo_lab2.controller;

import com.rbpo.rbpo_lab2.dto.AuthDto;
import com.rbpo.rbpo_lab2.model.AccountStatus;
import com.rbpo.rbpo_lab2.model.Role;
import com.rbpo.rbpo_lab2.model.User;
import com.rbpo.rbpo_lab2.repository.UserRepository;
import com.rbpo.rbpo_lab2.security.CurrentUser;
import com.rbpo.rbpo_lab2.security.PasswordPolicy;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final PasswordPolicy passwordPolicy;
    private final CurrentUser currentUser;

    public AuthController(UserRepository userRepository,
                          PasswordEncoder passwordEncoder,
                          PasswordPolicy passwordPolicy,
                          CurrentUser currentUser) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.passwordPolicy = passwordPolicy;
        this.currentUser = currentUser;
    }

    @GetMapping("/csrf")
    public CsrfToken getCsrfToken(CsrfToken token) {
        return token;
    }

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public AuthDto.MeResponse register(@Valid @RequestBody AuthDto.RegisterRequest req) {
        if (userRepository.findByUsername(req.username()).isPresent()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Имя пользователя уже занято");
        }

        passwordPolicy.validate(req.password(), req.username());

        User user = new User();
        user.setUsername(req.username());
        user.setEmail(req.email());
        user.setPasswordHash(passwordEncoder.encode(req.password()));
        user.setRole(Role.USER);
        user.setStatus(AccountStatus.ACTIVE);

        User saved = userRepository.save(user);
        return new AuthDto.MeResponse(saved.getId(), saved.getUsername(), saved.getEmail(), saved.getRole(), saved.getStatus());
    }

    @GetMapping("/me")
    public AuthDto.MeResponse me() {
        User u = currentUser.get();
        return new AuthDto.MeResponse(u.getId(), u.getUsername(), u.getEmail(), u.getRole(), u.getStatus());
    }
}