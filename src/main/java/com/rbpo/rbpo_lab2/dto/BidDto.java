package com.rbpo.rbpo_lab2.dto;

import java.math.BigDecimal;
import java.time.Instant;

public final class BidDto {
    private BidDto() {}
    /** Времени в запросе нет: его проставляет сервер. */
    public record CreateRequest(Long lotId, Long userId, BigDecimal amount) {}
    public record UpdateRequest(BigDecimal amount) {}
    public record Response(Long id, Long lotId, UserDto.Response user, BigDecimal amount, Instant createdAt) {}
}
