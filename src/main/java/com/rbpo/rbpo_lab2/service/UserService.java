package com.rbpo.rbpo_lab2.service;

import java.util.List;
import org.springframework.stereotype.Service;
import com.rbpo.rbpo_lab2.exception.ApiException;
import com.rbpo.rbpo_lab2.model.CollectibleItem;
import com.rbpo.rbpo_lab2.model.User;
import com.rbpo.rbpo_lab2.repository.UserRepository;

@Service
public class UserService {
    private final UserRepository users;
    private final CollectibleItemService items;
    private final BidService bids;

    public UserService(UserRepository users, CollectibleItemService items, BidService bids) {
        this.users = users;
        this.items = items;
        this.bids = bids;
    }

    public List<User> getAll() { return users.findAll(); }

    public User getById(Long id) {
        return users.findById(id).orElseThrow(() -> ApiException.notFound("Пользователь", id));
    }

    public User create(String username, String email) {
        requireText(username, "username");
        requireText(email, "email");
        return users.save(new User(users.nextId(), username, email));
    }

    public User update(Long id, String username, String email) {
        User old = getById(id);
        if (username != null) requireText(username, "username");
        if (email != null) requireText(email, "email");
        return users.save(new User(id,
                username != null ? username : old.username(),
                email != null ? email : old.email()));
    }

    public void delete(Long id) {
        getById(id);
        for (CollectibleItem item : items.findByOwner(id)) {
            items.delete(item.id());
        }
        bids.deleteByUser(id);
        users.deleteById(id);
    }

    private static void requireText(String value, String field) {
        if (value == null || value.isBlank()) {
            throw ApiException.badRequest("Поле " + field + " обязательно");
        }
    }
}
