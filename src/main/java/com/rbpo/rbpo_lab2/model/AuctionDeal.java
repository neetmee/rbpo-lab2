package com.rbpo.rbpo_lab2.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.util.Objects;

@Entity
@Table(name = "auction_deals")
public class AuctionDeal {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // На один лот не может быть больше одной сделки
    @OneToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "lot_id", nullable = false, unique = true, foreignKey = @ForeignKey(name = "fk_deals_lot"))
    private AuctionLot lot;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "bid_id", nullable = false, foreignKey = @ForeignKey(name = "fk_deals_bid"))
    private Bid bid;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal amount;

    @Enumerated(EnumType.STRING)
    @Column(name = "transfer_status", nullable = false, length = 20)
    private TransferStatus transferStatus = TransferStatus.PENDING;

    protected AuctionDeal() {}

    public AuctionDeal(AuctionLot lot, Bid bid, BigDecimal amount, TransferStatus transferStatus) {
        this.lot = lot;
        this.bid = bid;
        this.amount = amount;
        this.transferStatus = transferStatus != null ? transferStatus : TransferStatus.PENDING;
    }

    public AuctionDeal(Long id, AuctionLot lot, Bid bid, BigDecimal amount, TransferStatus transferStatus) {
        this.id = id;
        this.lot = lot;
        this.bid = bid;
        this.amount = amount;
        this.transferStatus = transferStatus != null ? transferStatus : TransferStatus.PENDING;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public AuctionLot getLot() { return lot; }
    public void setLot(AuctionLot lot) { this.lot = lot; }

    public Bid getBid() { return bid; }
    public void setBid(Bid bid) { this.bid = bid; }

    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }

    public TransferStatus getTransferStatus() { return transferStatus; }
    public void setTransferStatus(TransferStatus transferStatus) { this.transferStatus = transferStatus; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof AuctionDeal that)) return false;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
    public Long id() { return id; }
    public Long lotId() { return lot != null ? lot.getId() : null; }
    public Long bidId() { return bid != null ? bid.getId() : null; }
    public BigDecimal amount() { return amount; }
    public TransferStatus transferStatus() { return transferStatus; }
}
