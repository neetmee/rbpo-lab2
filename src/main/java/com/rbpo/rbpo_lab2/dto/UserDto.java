package com.rbpo.rbpo_lab2.dto;

public final class UserDto {
    private UserDto() {}
    public record CreateRequest(String username, String email) {}
    public record UpdateRequest(String username, String email) {}
    public record Response(Long id, String username, String email) {}
}
