package io.github.jaeykweon.jpaautoenumstring.integration;

import jakarta.persistence.*;

@Entity
@Table(name = "orders_with_embedded")
public class OrderWithEmbedded {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Embedded
    private ShippingInfo shippingInfo;

    protected OrderWithEmbedded() {
    }

    public OrderWithEmbedded(ShippingInfo shippingInfo) {
        this.shippingInfo = shippingInfo;
    }

    public Long getId() {
        return id;
    }

    public ShippingInfo getShippingInfo() {
        return shippingInfo;
    }
}
