package com.rbpo.rbpo_lab2.dto;

public class CollectibleItemDto {
    public record CreateRequest(String name, String description, Long ownerId, String condition) {}
    public record UpdateRequest(String name, String description, Long ownerId, String condition) {}
    public record Response(Long id, String name, String description, UserDto.Public owner, String condition) {}
}