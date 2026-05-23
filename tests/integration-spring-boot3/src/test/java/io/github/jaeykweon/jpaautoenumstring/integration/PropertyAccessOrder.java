package io.github.jaeykweon.jpaautoenumstring.integration;

import jakarta.persistence.*;

@Entity
@Table(name = "property_access_orders")
public class PropertyAccessOrder {

    // No annotations on fields — library sees no @Enumerated here
    private Long id;
    private OrderStatus status;

    protected PropertyAccessOrder() {}

    public PropertyAccessOrder(OrderStatus status) {
        this.status = status;
    }

    // @Id on getter triggers Hibernate property-based access for the whole entity
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    // User intended ORDINAL, but the library reads from fields — not getters — so this is ignored
    @Enumerated(EnumType.ORDINAL)
    public OrderStatus getStatus() { return status; }
    public void setStatus(OrderStatus status) { this.status = status; }
}
