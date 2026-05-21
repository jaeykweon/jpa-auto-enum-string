package io.github.jaeykweon.jpaautoenumstring.integration;

import com.example.external.ExternalOrder;
import io.github.jaeykweon.jpaautoenumstring.autoconfigure.JpaAutoEnumStringAutoConfiguration;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.TestPropertySource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

@DataJpaTest
@ImportAutoConfiguration(JpaAutoEnumStringAutoConfiguration.class)
@TestPropertySource(properties = "spring.jpa.hibernate.ddl-auto=create-drop")
class AutoEnumStringIntegrationTest {

    @Autowired OrderRepository orderRepository;
    @Autowired OrderWithConverterRepository orderWithConverterRepository;
    @Autowired OrderWithTypeRepository orderWithTypeRepository;
    @Autowired ExternalOrderRepository externalOrderRepository;
    @Autowired OrderWithMultipleStatusesRepository orderWithMultipleStatusesRepository;
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
    // Here the converter stores OrderStatus as a single-character code (e.g. "C"), not as the enum name.
    @Test
    void convertAnnotatedField_isNotOverriddenByLibrary() {
        orderWithConverterRepository.save(new OrderWithConverter(OrderStatus.CONFIRMED));

        String rawValue = jdbcTemplate.queryForObject(
            "SELECT status FROM orders_with_converter LIMIT 1", String.class
        );

        assertEquals("C", rawValue, "@Convert converter must remain in effect — not overridden by STRING mapping");
    }

    // @Type is an explicit user decision to use a custom Hibernate type mapping — the library must not override it.
    // OrdinalOrderStatusType stores OrderStatus as an integer ordinal; if the library overrode it, it would store STRING.
    @Test
    void typeAnnotatedField_isNotOverriddenByLibrary() {
        orderWithTypeRepository.save(new OrderWithType(OrderStatus.CONFIRMED));

        Number rawValue = jdbcTemplate.queryForObject(
            "SELECT status FROM orders_with_type LIMIT 1", Number.class
        );

        assertNotNull(rawValue);
        assertEquals(1, rawValue.intValue(),
            "@Type mapping must remain in effect — CONFIRMED is ordinal 1, not the string \"CONFIRMED\"");
    }

    @Test
    void nullEnumField_isStoredAsNull() {
        orderRepository.save(new Order("Order with no status", null));

        String rawValue = jdbcTemplate.queryForObject(
            "SELECT status FROM orders LIMIT 1", String.class
        );

        assertNull(rawValue, "Null enum should be stored as NULL in the database");
    }

    @Test
    void nullEnumField_isReadBackAsNull() {
        orderRepository.save(new Order("Order with no status", null));

        Order found = orderRepository.findAll().get(0);

        assertNull(found.getStatus(), "Null enum should be read back as null");
    }

    // Verifies that the library applies STRING mapping to all unannotated enum fields in an entity, not just the first.
    @Test
    void multipleUnannotatedEnumFields_areAllSavedAsString() {
        orderWithMultipleStatusesRepository.save(
            new OrderWithMultipleStatuses(OrderStatus.CONFIRMED, OrderStatus.SHIPPED));

        String primaryRaw = jdbcTemplate.queryForObject(
            "SELECT primary_status FROM orders_with_multiple_statuses LIMIT 1", String.class);
        String secondaryRaw = jdbcTemplate.queryForObject(
            "SELECT secondary_status FROM orders_with_multiple_statuses LIMIT 1", String.class);

        assertEquals("CONFIRMED", primaryRaw, "First enum field should be stored as STRING");
        assertEquals("SHIPPED", secondaryRaw, "Second enum field should be stored as STRING");
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
