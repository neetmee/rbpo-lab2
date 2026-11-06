package com.rbpo.rbpo_lab2.dto;

import java.math.BigDecimal;
import java.time.Instant;

public class BidDto {
    public record CreateRequest(Long lotId, Long userId, BigDecimal amount) {}
    public record UpdateRequest(BigDecimal amount) {}
    public record Response(Long id, Long lotId, UserDto.Public bidder, BigDecimal amount, Instant createdAt) {}
}