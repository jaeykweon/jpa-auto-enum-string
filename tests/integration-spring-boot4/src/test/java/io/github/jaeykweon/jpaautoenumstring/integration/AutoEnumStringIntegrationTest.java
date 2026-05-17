package io.github.jaeykweon.jpaautoenumstring.integration;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.TestPropertySource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@DataJpaTest
@Import(TestHibernate7Config.class)
@TestPropertySource(properties = "spring.jpa.hibernate.ddl-auto=create-drop")
class AutoEnumStringIntegrationTest {

    @Autowired OrderRepository orderRepository;
    @Autowired JdbcTemplate jdbcTemplate;

    @Test
    void unannotatedEnumField_isSavedAsString() {
        orderRepository.save(new Order("Test order", OrderStatus.CONFIRMED));

        String rawValue = jdbcTemplate.queryForObject(
            "SELECT status FROM orders LIMIT 1", String.class
        );

        assertEquals("CONFIRMED", rawValue, "Enum should be stored as STRING, not ordinal");
    }

    @Test
    void unannotatedEnumField_isReadBackCorrectly() {
        orderRepository.save(new Order("Another order", OrderStatus.SHIPPED));

        Order found = orderRepository.findAll().get(0);

        assertEquals(OrderStatus.SHIPPED, found.getStatus());
    }

    // @Enumerated(ORDINAL) is an explicit user decision — the library must not override it.
    @Test
    void explicitlyOrdinalAnnotatedField_remainsOrdinal() {
        orderRepository.save(new Order("Order", OrderStatus.CONFIRMED, OrderStatus.PENDING));

        Number rawValue = jdbcTemplate.queryForObject(
            "SELECT legacy_status FROM orders LIMIT 1", Number.class
        );

        assertNotNull(rawValue);
        assertEquals(0, rawValue.intValue(), "PENDING is ordinal 0 — must stay as ordinal");
    }
}
