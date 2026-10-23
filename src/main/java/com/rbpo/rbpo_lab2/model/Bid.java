package com.rbpo.rbpo_lab2.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.Objects;

@Entity
@Table(name = "bids")
public class Bid {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "lot_id", nullable = false, foreignKey = @ForeignKey(name = "fk_bids_lot"))
    private AuctionLot lot;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "user_id", nullable = false, foreignKey = @ForeignKey(name = "fk_bids_user"))
    private User user;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal amount;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    protected Bid() {}

    public Bid(AuctionLot lot, User user, BigDecimal amount, Instant createdAt) {
        this.lot = lot;
        this.user = user;
        this.amount = amount;
        this.createdAt = createdAt;
    }

    public Bid(Long id, AuctionLot lot, User user, BigDecimal amount, Instant createdAt) {
        this.id = id;
        this.lot = lot;
        this.user = user;
        this.amount = amount;
        this.createdAt = createdAt;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public AuctionLot getLot() { return lot; }
    public void setLot(AuctionLot lot) { this.lot = lot; }

    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }

    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }

    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Bid bid)) return false;
        return Objects.equals(id, bid.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
    public Long id() { return id; }
    public Long lotId() { return lot != null ? lot.getId() : null; }
    public Long userId() { return user != null ? user.getId() : null; }
    public BigDecimal amount() { return amount; }
    public Instant createdAt() { return createdAt; }
}