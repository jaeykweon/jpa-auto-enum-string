package com.example.manual.h6;

import jakarta.persistence.*;

@Entity
@Table(name = "orders")
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String description;

    private OrderStatus status;

    protected Order() {}

    public Order(String description, OrderStatus status) {
        this.description = description;
        this.status = status;
    }

    public Long getId() { return id; }
    public OrderStatus getStatus() { return status; }
}
