package com.rbpo.rbpo_lab2.dto;

import com.rbpo.rbpo_lab2.model.AccountStatus;
import com.rbpo.rbpo_lab2.model.Role;

public class UserDto {
    public record CreateRequest(String username, String email, String password, Role role) {}
    public record UpdateRequest(String username, String email, Role role, AccountStatus status) {}
    public record Response(Long id, String username, String email, Role role, AccountStatus status) {}
    public record Public(Long id, String username) {}
}