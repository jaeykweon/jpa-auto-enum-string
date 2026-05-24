package io.github.jaeykweon.jpaautoenumstring.integration;

import io.github.jaeykweon.jpaautoenumstring.autoconfigure.JpaAutoEnumStringAutoConfiguration;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.TestPropertySource;

import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@ImportAutoConfiguration(JpaAutoEnumStringAutoConfiguration.class)
@TestPropertySource(properties = "spring.jpa.hibernate.ddl-auto=create-drop")
class ElementCollectionTest {

    @Autowired OrderWithElementCollectionRepository repository;
    @Autowired JdbcTemplate jdbcTemplate;

    @Test
    void elementCollection_enumElements_areStoredAsString() {
        OrderWithElementCollection order = new OrderWithElementCollection(Set.of(OrderStatus.CONFIRMED, OrderStatus.SHIPPED));
        repository.saveAndFlush(order);

        List<String> rawValues = jdbcTemplate.queryForList(
            "SELECT status FROM order_element_statuses", String.class);

        assertFalse(rawValues.isEmpty());
        assertTrue(rawValues.stream().allMatch(v -> v.equals("CONFIRMED") || v.equals("SHIPPED")),
            "Enum elements should be stored as strings, not ordinals");
    }

    @Test
    void elementCollection_enumElements_areReadBackCorrectly() {
        OrderWithElementCollection order = new OrderWithElementCollection(Set.of(OrderStatus.PENDING));
        repository.saveAndFlush(order);

        OrderWithElementCollection found = repository.findAll().get(0);

        assertEquals(Set.of(OrderStatus.PENDING), found.getStatuses());
    }

    @Test
    void elementCollection_withExplicitOrdinal_staysOrdinal() {
        OrderWithElementCollection order = new OrderWithElementCollection(Set.of());
        order.addLegacyStatus(OrderStatus.PENDING);
        repository.saveAndFlush(order);

        List<Object> rawValues = jdbcTemplate.queryForList(
            "SELECT status FROM order_element_legacy_statuses", Object.class);

        assertFalse(rawValues.isEmpty());
        assertEquals(0, ((Number) rawValues.get(0)).intValue(),
            "PENDING ordinal is 0 — explicit @Enumerated(ORDINAL) must not be overridden");
    }
}
