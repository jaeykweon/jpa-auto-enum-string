package io.github.jaeykweon.jpaautoenumstring.integration;

import com.example.external.ExternalOrder;
import io.github.jaeykweon.jpaautoenumstring.autoconfigure.JpaAutoEnumStringAutoConfiguration;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.TestPropertySource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@DataJpaTest
@ImportAutoConfiguration(JpaAutoEnumStringAutoConfiguration.class)
@TestPropertySource(properties = "spring.jpa.hibernate.ddl-auto=create-drop")
class AutoEnumStringIntegrationTest {

    @Autowired OrderRepository orderRepository;
    @Autowired OrderWithConverterRepository orderWithConverterRepository;
    @Autowired ExternalOrderRepository externalOrderRepository;
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

    // @Convert is an explicit user decision to use a custom converter — the library must not override it.
    // Disabled due to a known H2 2.4.x bug: Hibernate 7 generates a CHECK constraint for @Convert fields
    // (e.g. CHECK (status IN ('P','C','S','X'))), and H2 2.4.x fails to evaluate it on INSERT.
    // This is an H2-side regression fixed in H2 PR #4311 (merged Nov 2025) but not yet released.
    // Re-enable once H2 ships a release containing that fix.
    // See: https://github.com/h2database/h2database/issues/4302
    @Test
    @Disabled("H2 2.4.x cannot evaluate Hibernate 7 CHECK constraints on @Convert fields — H2 issue #4302")
    void convertAnnotatedField_isNotOverriddenByLibrary() {
        orderWithConverterRepository.save(new OrderWithConverter(OrderStatus.CONFIRMED));

        String rawValue = jdbcTemplate.queryForObject(
            "SELECT status FROM orders_with_converter LIMIT 1", String.class
        );

        assertEquals("C", rawValue, "@Convert converter must remain in effect — not overridden by STRING mapping");
    }

    // ExternalOrder is in a sub-package outside the base package configured by @SpringBootApplication.
    // The library should not apply STRING mapping to entities outside the scan scope.
    @Test
    void entityOutsideBasePackage_enumIsNotMappedAsString() {
        externalOrderRepository.save(new ExternalOrder(OrderStatus.CONFIRMED));

        Number rawValue = jdbcTemplate.queryForObject(
            "SELECT status FROM external_orders LIMIT 1", Number.class
        );

        assertNotNull(rawValue);
        assertEquals(1, rawValue.intValue(), "Entity outside base package should be stored as ordinal, not STRING");
    }
}
