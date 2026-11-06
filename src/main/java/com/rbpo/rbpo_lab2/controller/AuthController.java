package com.rbpo.rbpo_lab2.controller;

import com.rbpo.rbpo_lab2.dto.AuthDto;
import com.rbpo.rbpo_lab2.dto.UserDto;
import com.rbpo.rbpo_lab2.exception.ApiException;
import com.rbpo.rbpo_lab2.model.AccountStatus;
import com.rbpo.rbpo_lab2.model.Role;
import com.rbpo.rbpo_lab2.model.User;
import com.rbpo.rbpo_lab2.repository.UserRepository;
import com.rbpo.rbpo_lab2.security.PasswordPolicy;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final PasswordPolicy passwordPolicy;

    // Рекорд для ответа метода /me
    public record MeResponse(Long id, String username, String email, Role role, AccountStatus status) {}

    public AuthController(UserRepository userRepository,
                          PasswordEncoder passwordEncoder,
                          PasswordPolicy passwordPolicy) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.passwordPolicy = passwordPolicy;
    }

    @GetMapping("/csrf")
    public AuthDto.CsrfResponse csrf(CsrfToken token) {
        return new AuthDto.CsrfResponse(token.getToken(), token.getHeaderName());
    }

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public UserDto.Response register(@Valid @RequestBody AuthDto.RegisterRequest req) {
        if (userRepository.existsByUsername(req.username())) {
            throw ApiException.conflict("Имя пользователя уже занято");
        }
        if (userRepository.existsByEmail(req.email())) {
            throw ApiException.conflict("Email уже используется");
        }
        passwordPolicy.validate(req.password(), req.username());

        String hash = passwordEncoder.encode(req.password());
        User user = new User(null, req.username(), req.email(), hash, Role.USER, AccountStatus.ACTIVE);
        User saved = userRepository.save(user);

        return new UserDto.Response(saved.getId(), saved.getUsername(), saved.getEmail(), saved.getRole(), saved.getStatus());
    }

    @GetMapping("/me")
    public MeResponse me(Authentication auth) {
        User user = userRepository.findByUsername(auth.getName())
                .orElseThrow(() -> ApiException.notFound("Пользователь", 0L));
        return new MeResponse(user.getId(), user.getUsername(), user.getEmail(), user.getRole(), user.getStatus());
    }
}