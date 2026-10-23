package com.rbpo.rbpo_lab2.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.Objects;

@Entity
@Table(name = "auction_lots")
public class AuctionLot {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "item_id", nullable = false, foreignKey = @ForeignKey(name = "fk_lots_item"))
    private CollectibleItem item;

    @Column(name = "start_price", nullable = false, precision = 19, scale = 2)
    private BigDecimal startPrice;

    @Column(name = "bid_step", nullable = false, precision = 19, scale = 2)
    private BigDecimal bidStep;

    @Column(name = "start_time", nullable = false)
    private Instant startTime;

    @Column(name = "end_time", nullable = false)
    private Instant endTime;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private LotStatus status = LotStatus.CREATED;

    protected AuctionLot() {}

    public AuctionLot(CollectibleItem item, BigDecimal startPrice, BigDecimal bidStep,
                      Instant startTime, Instant endTime, LotStatus status) {
        this.item = item;
        this.startPrice = startPrice;
        this.bidStep = bidStep;
        this.startTime = startTime;
        this.endTime = endTime;
        this.status = status != null ? status : LotStatus.CREATED;
    }

    public AuctionLot(Long id, CollectibleItem item, BigDecimal startPrice, BigDecimal bidStep,
                      Instant startTime, Instant endTime, LotStatus status) {
        this.id = id;
        this.item = item;
        this.startPrice = startPrice;
        this.bidStep = bidStep;
        this.startTime = startTime;
        this.endTime = endTime;
        this.status = status != null ? status : LotStatus.CREATED;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public CollectibleItem getItem() { return item; }
    public void setItem(CollectibleItem item) { this.item = item; }

    public BigDecimal getStartPrice() { return startPrice; }
    public void setStartPrice(BigDecimal startPrice) { this.startPrice = startPrice; }

    public BigDecimal getBidStep() { return bidStep; }
    public void setBidStep(BigDecimal bidStep) { this.bidStep = bidStep; }

    public Instant getStartTime() { return startTime; }
    public void setStartTime(Instant startTime) { this.startTime = startTime; }

    public Instant getEndTime() { return endTime; }
    public void setEndTime(Instant endTime) { this.endTime = endTime; }

    public LotStatus getStatus() { return status; }
    public void setStatus(LotStatus status) { this.status = status; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof AuctionLot that)) return false;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
    public Long id() { return id; }
    public Long itemId() { return item != null ? item.getId() : null; }
    public BigDecimal startPrice() { return startPrice; }
    public BigDecimal bidStep() { return bidStep; }
    public Instant startTime() { return startTime; }
    public Instant endTime() { return endTime; }
    public LotStatus status() { return status; }
}