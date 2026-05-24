package io.github.jaeykweon.jpaautoenumstring.integration;

import javax.persistence.Embeddable;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;

import java.util.Objects;

@Embeddable
public class OrderStatusEntry {

    private OrderStatus status;

    @Enumerated(EnumType.ORDINAL)
    private OrderStatus legacyStatus;

    protected OrderStatusEntry() {}

    public OrderStatusEntry(OrderStatus status, OrderStatus legacyStatus) {
        this.status = status;
        this.legacyStatus = legacyStatus;
    }

    public OrderStatus getStatus() { return status; }
    public OrderStatus getLegacyStatus() { return legacyStatus; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof OrderStatusEntry)) return false;
        OrderStatusEntry that = (OrderStatusEntry) o;
        return status == that.status && legacyStatus == that.legacyStatus;
    }

    @Override
    public int hashCode() {
        return Objects.hash(status, legacyStatus);
    }
}
