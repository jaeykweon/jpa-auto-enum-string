package io.github.jaeykweon.jpaautoenumstring.integration;

import io.github.jaeykweon.jpaautoenumstring.autoconfigure.JpaAutoEnumStringAutoConfiguration;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.TestPropertySource;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;

@DataJpaTest
@ImportAutoConfiguration(JpaAutoEnumStringAutoConfiguration.class)
@TestPropertySource(properties = "spring.jpa.hibernate.ddl-auto=create-drop")
class ElementCollectionEmbeddableTest {

    @Autowired OrderWithEmbeddableCollectionRepository repository;
    @Autowired JdbcTemplate jdbcTemplate;

    @Test
    void embeddableElementCollection_unannotatedEnumField_isStoredAsString() {
        OrderStatusEntry entry = new OrderStatusEntry(OrderStatus.CONFIRMED, OrderStatus.PENDING);
        repository.saveAndFlush(new OrderWithEmbeddableCollection(Set.of(entry)));

        String rawStatus = jdbcTemplate.queryForObject(
            "SELECT status FROM order_status_entries LIMIT 1", String.class);

        assertEquals("CONFIRMED", rawStatus, "Unannotated enum inside embeddable element should be stored as STRING");
    }

    @Test
    void embeddableElementCollection_explicitOrdinalField_staysOrdinal() {
        OrderStatusEntry entry = new OrderStatusEntry(OrderStatus.CONFIRMED, OrderStatus.PENDING);
        repository.saveAndFlush(new OrderWithEmbeddableCollection(Set.of(entry)));

        Number rawLegacy = jdbcTemplate.queryForObject(
            "SELECT legacy_status FROM order_status_entries LIMIT 1", Number.class);

        assertEquals(0, rawLegacy.intValue(), "PENDING ordinal is 0 — explicit @Enumerated(ORDINAL) must not be overridden");
    }

    @Test
    void embeddableElementCollection_isReadBackCorrectly() {
        OrderStatusEntry entry = new OrderStatusEntry(OrderStatus.SHIPPED, OrderStatus.CANCELLED);
        repository.saveAndFlush(new OrderWithEmbeddableCollection(Set.of(entry)));

        OrderWithEmbeddableCollection found = repository.findAll().get(0);

        assertEquals(Set.of(entry), found.getEntries());
    }
}
