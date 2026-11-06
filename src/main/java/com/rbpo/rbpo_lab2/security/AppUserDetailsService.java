package com.rbpo.rbpo_lab2.security;

import com.rbpo.rbpo_lab2.model.AccountStatus;
import com.rbpo.rbpo_lab2.model.User;
import com.rbpo.rbpo_lab2.repository.UserRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class AppUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    public AppUserDetailsService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("Пользователь " + username + " не найден"));

        return org.springframework.security.core.userdetails.User.builder()
                .username(user.getUsername())
                .password(user.getPasswordHash())
                .authorities(user.getAuthorities())
                .disabled(user.getStatus() != AccountStatus.ACTIVE)
                .accountLocked(user.getStatus() != AccountStatus.ACTIVE)
                .build();
    }
}