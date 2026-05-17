package com.example.external;

import io.github.jaeykweon.jpaautoenumstring.integration.OrderStatus;

import javax.persistence.*;

// Entity outside the library's base package scope (com.example.external vs io.github.jaeykweon.*).
// Used to verify that the library does not apply STRING mapping to out-of-scope entities.
@Entity
@Table(name = "external_orders")
public class ExternalOrder {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private OrderStatus status;

    protected ExternalOrder() {
    }

    public ExternalOrder(OrderStatus status) {
        this.status = status;
    }

    public Long getId() {
        return id;
    }

    public OrderStatus getStatus() {
        return status;
    }
}
