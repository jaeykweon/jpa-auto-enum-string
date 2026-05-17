package io.github.jaeykweon.jpaautoenumstring.integration;

import jakarta.persistence.*;

@Entity
@Table(name = "orders")
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String description;

    // No @Enumerated annotation — library should apply STRING mapping automatically
    private OrderStatus status;

    // Explicitly annotated as ORDINAL — library must not override this
    @Enumerated(EnumType.ORDINAL)
    private OrderStatus legacyStatus;

    protected Order() {
    }

    public Order(String description, OrderStatus status) {
        this.description = description;
        this.status = status;
    }

    public Order(String description, OrderStatus status, OrderStatus legacyStatus) {
        this.description = description;
        this.status = status;
        this.legacyStatus = legacyStatus;
    }

    public Long getId() { return id; }
    public String getDescription() { return description; }
    public OrderStatus getStatus() { return status; }
    public OrderStatus getLegacyStatus() { return legacyStatus; }
}
