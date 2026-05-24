package io.github.jaeykweon.jpaautoenumstring.integration;

import javax.persistence.*;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "orders_with_embeddable_collection")
public class OrderWithEmbeddableCollection {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ElementCollection
    @CollectionTable(name = "order_status_entries", joinColumns = @JoinColumn(name = "order_id"))
    private Set<OrderStatusEntry> entries = new HashSet<>();

    protected OrderWithEmbeddableCollection() {}

    public OrderWithEmbeddableCollection(Set<OrderStatusEntry> entries) {
        this.entries = new HashSet<>(entries);
    }

    public Long getId() { return id; }
    public Set<OrderStatusEntry> getEntries() { return entries; }
}
