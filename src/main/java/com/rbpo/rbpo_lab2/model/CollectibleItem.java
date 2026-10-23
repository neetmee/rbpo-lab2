package com.rbpo.rbpo_lab2.model;

import jakarta.persistence.*;
import java.util.Objects;

@Entity
@Table(name = "collectible_items")
public class CollectibleItem {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 200)
    private String name;

    @Column(length = 2000)
    private String description;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "owner_id", nullable = false, foreignKey = @ForeignKey(name = "fk_items_owner"))
    private User owner;

    @Column(name = "item_condition", nullable = false, length = 100)
    private String condition;

    protected CollectibleItem() {}

    public CollectibleItem(String name, String description, User owner, String condition) {
        this.name = name;
        this.description = description;
        this.owner = owner;
        this.condition = condition;
    }

    public CollectibleItem(Long id, String name, String description, User owner, String condition) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.owner = owner;
        this.condition = condition;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public User getOwner() { return owner; }
    public void setOwner(User owner) { this.owner = owner; }

    public String getCondition() { return condition; }
    public void setCondition(String condition) { this.condition = condition; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof CollectibleItem item)) return false;
        return Objects.equals(id, item.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
    public Long id() { return id; }
    public String name() { return name; }
    public String description() { return description; }
    public Long ownerId() { return owner != null ? owner.getId() : null; }
    public String condition() { return condition; }
}