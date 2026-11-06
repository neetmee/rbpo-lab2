package com.rbpo.rbpo_lab2.config;

import com.rbpo.rbpo_lab2.model.AccountStatus;
import com.rbpo.rbpo_lab2.model.Role;
import com.rbpo.rbpo_lab2.model.User;
import com.rbpo.rbpo_lab2.repository.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class AdminSeeder implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${app.admin.password:AdminPass123!}")
    private String adminPassword;

    public AdminSeeder(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        if (userRepository.findByUsername("admin").isEmpty()) {
            User admin = new User();
            admin.setUsername("admin");
            admin.setEmail("admin@auction.local");
            admin.setPasswordHash(passwordEncoder.encode(adminPassword));
            admin.setRole(Role.ADMIN);
            admin.setStatus(AccountStatus.ACTIVE);
            userRepository.save(admin);
        }
    }
}