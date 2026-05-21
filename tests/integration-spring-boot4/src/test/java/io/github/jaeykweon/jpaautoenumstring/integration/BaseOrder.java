package io.github.jaeykweon.jpaautoenumstring.integration;

import jakarta.persistence.*;

@MappedSuperclass
public abstract class BaseOrder {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // No @Enumerated — the library should apply STRING mapping to fields inherited from @MappedSuperclass
    private OrderStatus status;

    protected BaseOrder() {}

    public BaseOrder(OrderStatus status) {
        this.status = status;
    }

    public Long getId() { return id; }
    public OrderStatus getStatus() { return status; }
}
