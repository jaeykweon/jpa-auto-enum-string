package io.github.jaeykweon.jpaautoenumstring.integration;

import jakarta.persistence.*;
import org.hibernate.annotations.Type;

@Entity
@Table(name = "orders_with_type")
public class OrderWithType {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // @Type is an explicit user decision to use a custom Hibernate type mapping —
    // the library must not override it.
    @Type(OrdinalOrderStatusType.class)
    private OrderStatus status;

    protected OrderWithType() {}

    public OrderWithType(OrderStatus status) {
        this.status = status;
    }

    public Long getId() { return id; }
    public OrderStatus getStatus() { return status; }
}
