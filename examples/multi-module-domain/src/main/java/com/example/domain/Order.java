package com.example.domain;

import jakarta.persistence.*;

@Entity
@Table(name = "orders")
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String description;

    // No @Enumerated — jpa-auto-enum-string should apply STRING mapping
    private OrderStatus status;

    protected Order() {}

    public Order(String description, OrderStatus status) {
        this.description = description;
        this.status = status;
    }

    public Long getId() { return id; }
    public String getDescription() { return description; }
    public OrderStatus getStatus() { return status; }
}
