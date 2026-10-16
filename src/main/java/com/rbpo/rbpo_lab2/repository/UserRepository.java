package com.rbpo.rbpo_lab2.repository;

import org.springframework.stereotype.Repository;
import com.rbpo.rbpo_lab2.model.User;

@Repository
public class UserRepository extends InMemoryRepository<User> {}
