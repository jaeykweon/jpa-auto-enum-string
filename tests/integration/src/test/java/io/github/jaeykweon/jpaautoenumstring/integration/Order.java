package io.github.jaeykweon.jpaautoenumstring.integration;

import javax.persistence.*;

@Entity
@Table(name = "orders")
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String description;

    // No @Enumerated annotation — library should apply STRING mapping automatically
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
