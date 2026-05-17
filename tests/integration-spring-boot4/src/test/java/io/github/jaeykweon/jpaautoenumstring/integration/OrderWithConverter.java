package io.github.jaeykweon.jpaautoenumstring.integration;

import jakarta.persistence.*;

@Entity
@Table(name = "orders_with_converter")
public class OrderWithConverter {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // @Convert is an explicit user decision to use a custom converter —
    // the library must not override it.
    @Convert(converter = OrderStatusConverter.class)
    private OrderStatus status;

    protected OrderWithConverter() {
    }

    public OrderWithConverter(OrderStatus status) {
        this.status = status;
    }

    public Long getId() { return id; }
    public OrderStatus getStatus() { return status; }
}
