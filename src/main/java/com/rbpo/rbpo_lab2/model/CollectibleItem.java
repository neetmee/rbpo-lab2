package com.rbpo.rbpo_lab2.model;

public record CollectibleItem(Long id, String name, String description, Long ownerId, String condition)
        implements Identifiable {}
