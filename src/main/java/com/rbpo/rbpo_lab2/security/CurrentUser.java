package com.rbpo.rbpo_lab2.security;

import com.rbpo.rbpo_lab2.model.Role;
import com.rbpo.rbpo_lab2.model.User;
import com.rbpo.rbpo_lab2.repository.UserRepository;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;

@Component
public class CurrentUser {

    private final UserRepository userRepository;

    public CurrentUser(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public User get() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getPrincipal())) {
            throw new IllegalStateException("Пользователь не аутентифицирован");
        }
        return userRepository.findByUsername(auth.getName())
                .orElseThrow(() -> new UsernameNotFoundException("Текущий пользователь не найден в БД"));
    }

    public Long id() {
        return get().getId();
    }

    public boolean isAdmin() {
        return get().getRole() == Role.ADMIN;
    }
}