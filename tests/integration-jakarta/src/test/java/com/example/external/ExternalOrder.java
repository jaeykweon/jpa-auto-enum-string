package com.example.external;

import io.github.jaeykweon.jpaautoenumstring.integration.OrderStatus;
import jakarta.persistence.*;

@Entity
@Table(name = "external_orders")
public class ExternalOrder {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // No @Enumerated annotation — but this entity is outside the base package,
    // so the library must not apply STRING mapping here.
    private OrderStatus status;

    protected ExternalOrder() {
    }

    public ExternalOrder(OrderStatus status) {
        this.status = status;
    }

    public Long getId() { return id; }
    public OrderStatus getStatus() { return status; }
}
