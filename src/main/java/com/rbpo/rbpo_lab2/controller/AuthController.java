package com.rbpo.rbpo_lab2.controller;

import com.rbpo.rbpo_lab2.dto.AuthDto;
import com.rbpo.rbpo_lab2.exception.ApiException;
import com.rbpo.rbpo_lab2.model.AccountStatus;
import com.rbpo.rbpo_lab2.model.Role;
import com.rbpo.rbpo_lab2.model.User;
import com.rbpo.rbpo_lab2.repository.UserRepository;
import com.rbpo.rbpo_lab2.service.TokenSessionService;
import com.rbpo.rbpo_lab2.service.UserService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final UserRepository userRepository;
    private final UserService userService;
    private final TokenSessionService tokenSessionService;
    private final AuthenticationManager authenticationManager;

    public record MeResponse(Long id, String username, String email, Role role, AccountStatus status) {}

    public AuthController(
            UserRepository userRepository,
            UserService userService,
            TokenSessionService tokenSessionService,
            AuthenticationManager authenticationManager
    ) {
        this.userRepository = userRepository;
        this.userService = userService;
        this.tokenSessionService = tokenSessionService;
        this.authenticationManager = authenticationManager;
    }

    @PostMapping("/login")
    public AuthDto.TokenPairResponse login(@Valid @RequestBody AuthDto.LoginRequest req) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(req.username(), req.password())
        );
        User user = userRepository.findByUsername(req.username())
                .orElseThrow(() -> ApiException.unauthorized("Ошибка аутентификации"));
        TokenSessionService.TokenPair pair = tokenSessionService.issuePair(user, req.deviceId());
        return new AuthDto.TokenPairResponse(pair.accessToken(), pair.refreshToken());
    }

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public AuthDto.TokenPairResponse register(@Valid @RequestBody AuthDto.RegisterRequest req) {
        TokenSessionService.TokenPair pair = userService.register(req, tokenSessionService);
        return new AuthDto.TokenPairResponse(pair.accessToken(), pair.refreshToken());
    }

    @PostMapping("/refresh")
    public AuthDto.TokenPairResponse refresh(@Valid @RequestBody AuthDto.RefreshRequest req) {
        TokenSessionService.TokenPair pair = tokenSessionService.refresh(req.refreshToken());
        return new AuthDto.TokenPairResponse(pair.accessToken(), pair.refreshToken());
    }

    @GetMapping("/me")
    public MeResponse me(Authentication auth) {
        User user = userRepository.findByUsername(auth.getName())
                .orElseThrow(() -> ApiException.notFound("Пользователь", 0L));
        return new MeResponse(user.getId(), user.getUsername(), user.getEmail(), user.getRole(), user.getStatus());
    }
}