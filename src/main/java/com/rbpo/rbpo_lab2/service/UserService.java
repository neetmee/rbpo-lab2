package com.rbpo.rbpo_lab2.service;

import com.rbpo.rbpo_lab2.exception.ApiException;
import com.rbpo.rbpo_lab2.model.CollectibleItem;
import com.rbpo.rbpo_lab2.model.User;
import com.rbpo.rbpo_lab2.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class UserService {

    private final UserRepository users;
    private final CollectibleItemService items;
    private final BidService bids;

    public UserService(UserRepository users, CollectibleItemService items, BidService bids) {
        this.users = users;
        this.items = items;
        this.bids = bids;
    }

    @Transactional(readOnly = true)
    public List<User> getAll() {
        return users.findAll();
    }

    @Transactional(readOnly = true)
    public User getById(Long id) {
        return users.findById(id)
                .orElseThrow(() -> ApiException.notFound("Пользователь", id));
    }

    public User create(String username, String email) {
        requireText(username, "username");
        requireText(email, "email");

        if (users.existsByUsername(username)) {
            throw ApiException.conflict("Пользователь с username '" + username + "' уже существует");
        }
        if (users.existsByEmail(email)) {
            throw ApiException.conflict("Пользователь с таким email уже существует");
        }

        return users.save(new User(username, email));
    }

    public User update(Long id, String username, String email) {
        User user = getById(id);

        if (username != null) {
            requireText(username, "username");
            if (!username.equals(user.getUsername()) && users.existsByUsername(username)) {
                throw ApiException.conflict("Username '" + username + "' уже занят");
            }
            user.setUsername(username);
        }

        if (email != null) {
            requireText(email, "email");
            if (!email.equals(user.getEmail()) && users.existsByEmail(email)) {
                throw ApiException.conflict("Пользователь с таким email уже существует");
            }
            user.setEmail(email);
        }

        return users.save(user);
    }

    public void delete(Long id) {
        User user = getById(id);
        // Каскадное удаление предметов (с их лотами и сделками)
        for (CollectibleItem item : items.findByOwner(id)) {
            items.delete(item.getId());
        }
        // Удаление ставок пользователя как покупателя
        bids.deleteByUser(id);
        users.delete(user);
    }

    private static void requireText(String value, String field) {
        if (value == null || value.isBlank()) {
            throw ApiException.badRequest("Поле " + field + " обязательно");
        }
    }
}