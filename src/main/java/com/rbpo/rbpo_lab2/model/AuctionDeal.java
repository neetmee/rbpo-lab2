package com.rbpo.rbpo_lab2.model;

import java.math.BigDecimal;

public record AuctionDeal(Long id, Long lotId, Long bidId, BigDecimal amount, TransferStatus transferStatus)
        implements Identifiable {}
