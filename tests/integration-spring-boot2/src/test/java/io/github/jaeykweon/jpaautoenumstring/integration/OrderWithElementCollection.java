package io.github.jaeykweon.jpaautoenumstring.integration;

import javax.persistence.*;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "orders_with_element_collection")
public class OrderWithElementCollection {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ElementCollection
    @CollectionTable(name = "order_element_statuses", joinColumns = @JoinColumn(name = "order_id"))
    @Column(name = "status")
    private Set<OrderStatus> statuses = new HashSet<>();

    @ElementCollection
    @CollectionTable(name = "order_element_legacy_statuses", joinColumns = @JoinColumn(name = "order_id"))
    @Column(name = "status")
    @Enumerated(EnumType.ORDINAL)
    private Set<OrderStatus> legacyStatuses = new HashSet<>();

    protected OrderWithElementCollection() {}

    public OrderWithElementCollection(Set<OrderStatus> statuses) {
        this.statuses = statuses;
    }

    public Long getId() { return id; }
    public Set<OrderStatus> getStatuses() { return statuses; }
    public Set<OrderStatus> getLegacyStatuses() { return legacyStatuses; }

    public void addLegacyStatus(OrderStatus status) { legacyStatuses.add(status); }
}
