package io.github.jaeykweon.jpaautoenumstring.integration;

import javax.persistence.*;

@Entity
@Table(name = "derived_orders")
public class DerivedOrder extends BaseOrder {

    protected DerivedOrder() {}

    public DerivedOrder(OrderStatus status) {
        super(status);
    }
}
