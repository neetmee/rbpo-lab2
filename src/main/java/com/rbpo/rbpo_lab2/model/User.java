package com.rbpo.rbpo_lab2.model;

public record User(Long id, String username, String email) implements Identifiable {}
