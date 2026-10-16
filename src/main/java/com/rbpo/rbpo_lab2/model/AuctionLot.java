package com.rbpo.rbpo_lab2.model;

import java.math.BigDecimal;
import java.time.Instant;

public record AuctionLot(Long id, Long itemId, BigDecimal startPrice, BigDecimal bidStep,
                         Instant startTime, Instant endTime, LotStatus status) implements Identifiable {}
