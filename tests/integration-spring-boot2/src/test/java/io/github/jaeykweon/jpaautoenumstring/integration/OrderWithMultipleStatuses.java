package io.github.jaeykweon.jpaautoenumstring.integration;

import javax.persistence.*;

@Entity
@Table(name = "orders_with_multiple_statuses")
public class OrderWithMultipleStatuses {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Both fields unannotated — the library should apply STRING to all enum fields, not just the first one
    private OrderStatus primaryStatus;
    private OrderStatus secondaryStatus;

    protected OrderWithMultipleStatuses() {}

    public OrderWithMultipleStatuses(OrderStatus primaryStatus, OrderStatus secondaryStatus) {
        this.primaryStatus = primaryStatus;
        this.secondaryStatus = secondaryStatus;
    }

    public Long getId() { return id; }
    public OrderStatus getPrimaryStatus() { return primaryStatus; }
    public OrderStatus getSecondaryStatus() { return secondaryStatus; }
}
