package com.rbpo.rbpo_lab2.dto;

import java.math.BigDecimal;
import com.rbpo.rbpo_lab2.model.TransferStatus;

public final class AuctionDealDto {
    private AuctionDealDto() {}
    public record CreateRequest(Long lotId, Long bidId, TransferStatus transferStatus) {}
    public record UpdateRequest(TransferStatus transferStatus) {}
    public record Response(Long id, Long lotId, Long bidId, BigDecimal amount, TransferStatus transferStatus) {}
}
