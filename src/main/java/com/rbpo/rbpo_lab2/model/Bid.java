package com.rbpo.rbpo_lab2.model;

import java.math.BigDecimal;
import java.time.Instant;

public record Bid(Long id, Long lotId, Long userId, BigDecimal amount, Instant createdAt)
        implements Identifiable {}
