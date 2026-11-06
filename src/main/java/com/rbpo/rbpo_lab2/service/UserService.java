package com.rbpo.rbpo_lab2.service;

import java.util.List;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.rbpo.rbpo_lab2.exception.ApiException;
import com.rbpo.rbpo_lab2.model.AccountStatus;
import com.rbpo.rbpo_lab2.model.Role;
import com.rbpo.rbpo_lab2.model.User;
import com.rbpo.rbpo_lab2.repository.BidRepository;
import com.rbpo.rbpo_lab2.repository.CollectibleItemRepository;
import com.rbpo.rbpo_lab2.repository.UserRepository;
import com.rbpo.rbpo_lab2.security.PasswordPolicy;

@Service
public class UserService {

    private final UserRepository users;
    private final CollectibleItemRepository itemRepo;
    private final BidRepository bidRepo;
    private final PasswordPolicy passwordPolicy;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository users,
                       CollectibleItemRepository itemRepo,
                       BidRepository bidRepo,
                       PasswordPolicy passwordPolicy,
                       PasswordEncoder passwordEncoder) {
        this.users = users;
        this.itemRepo = itemRepo;
        this.bidRepo = bidRepo;
        this.passwordPolicy = passwordPolicy;
        this.passwordEncoder = passwordEncoder;
    }

    public List<User> getAll() {
        return users.findAll();
    }

    public User getById(Long id) {
        return users.findById(id)
                .orElseThrow(() -> ApiException.notFound("Пользователь", id));
    }

    public User create(String username, String email, String rawPassword, Role role) {
        requireText(username, "username");
        requireText(email, "email");
        if (rawPassword == null || rawPassword.isBlank()) {
            throw ApiException.badRequest("Поле 'password' обязательно для заполнения");
        }

        if (users.existsByUsername(username)) {
            throw ApiException.conflict("Пользователь с username '" + username + "' уже существует");
        }
        if (users.existsByEmail(email)) {
            throw ApiException.conflict("Пользователь с таким email уже существует");
        }

        passwordPolicy.validate(rawPassword, username);
        String hash = passwordEncoder.encode(rawPassword);
        Role userRole = role != null ? role : Role.USER;

        return users.save(new User(null, username, email, hash, userRole, AccountStatus.ACTIVE));
    }

    public User update(Long id, String username, String email, Role role, AccountStatus status) {
        User existing = getById(id);
        String newUsername = (username != null && !username.isBlank()) ? username : existing.getUsername();
        String newEmail = (email != null && !email.isBlank()) ? email : existing.getEmail();
        Role newRole = role != null ? role : existing.getRole();
        AccountStatus newStatus = status != null ? status : existing.getStatus();

        if (username != null && !username.equals(existing.getUsername()) && users.existsByUsername(username)) {
            throw ApiException.conflict("Пользователь с username '" + username + "' уже существует");
        }
        if (email != null && !email.equals(existing.getEmail()) && users.existsByEmail(email)) {
            throw ApiException.conflict("Пользователь с таким email уже существует");
        }

        return users.save(new User(existing.getId(), newUsername, newEmail,
                existing.getPassword(), newRole, newStatus));
    }

    @Transactional
    public void delete(Long id) {
        User user = getById(id);
        bidRepo.findAll().stream()
                .filter(b -> b.userId().equals(id))
                .forEach(b -> bidRepo.deleteById(b.id()));

        itemRepo.findAll().stream()
                .filter(i -> i.ownerId().equals(id))
                .forEach(i -> itemRepo.deleteById(i.id()));

        users.delete(user);
    }

    private void requireText(String value, String field) {
        if (value == null || value.trim().isEmpty()) {
            throw ApiException.badRequest("Поле '" + field + "' обязательно для заполнения");
        }
    }
}