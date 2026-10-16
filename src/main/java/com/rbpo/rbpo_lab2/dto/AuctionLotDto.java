package com.rbpo.rbpo_lab2.dto;

import java.math.BigDecimal;
import java.time.Instant;
import com.rbpo.rbpo_lab2.model.LotStatus;

public final class AuctionLotDto {
    private AuctionLotDto() {}
    public record CreateRequest(Long itemId, BigDecimal startPrice, BigDecimal bidStep,
                                Instant startTime, Instant endTime) {}
    public record UpdateRequest(BigDecimal startPrice, BigDecimal bidStep,
                                Instant startTime, Instant endTime, LotStatus status) {}
    public record Response(Long id, Long itemId, BigDecimal startPrice, BigDecimal bidStep,
                           Instant startTime, Instant endTime, LotStatus status) {}
}
