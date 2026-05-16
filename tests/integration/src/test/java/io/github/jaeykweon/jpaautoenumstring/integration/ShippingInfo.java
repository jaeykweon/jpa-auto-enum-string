package io.github.jaeykweon.jpaautoenumstring.integration;

import javax.persistence.Embeddable;
import javax.persistence.Enumerated;
import javax.persistence.EnumType;

@Embeddable
public class ShippingInfo {

    // No @Enumerated — library should apply STRING mapping automatically
    private OrderStatus status;

    // Explicitly ORDINAL — library must not override
    @Enumerated(EnumType.ORDINAL)
    private OrderStatus legacyStatus;

    protected ShippingInfo() {
    }

    public ShippingInfo(OrderStatus status) {
        this.status = status;
    }

    public ShippingInfo(OrderStatus status, OrderStatus legacyStatus) {
        this.status = status;
        this.legacyStatus = legacyStatus;
    }

    public OrderStatus getStatus() {
        return status;
    }

    public OrderStatus getLegacyStatus() {
        return legacyStatus;
    }
}
