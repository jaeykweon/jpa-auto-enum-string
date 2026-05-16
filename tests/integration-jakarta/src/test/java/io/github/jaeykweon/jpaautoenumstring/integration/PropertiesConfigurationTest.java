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

// Verifies that jpa.auto-enum-string.base-packages overrides the auto-detected packages.
@DataJpaTest
@ImportAutoConfiguration(JpaAutoEnumStringAutoConfiguration.class)
@TestPropertySource(properties = {
    "spring.jpa.hibernate.ddl-auto=create-drop",
    "jpa.auto-enum-string.base-packages=com.example.external"
})
class PropertiesConfigurationTest {

    @Autowired ExternalOrderRepository externalOrderRepository;
    @Autowired OrderRepository orderRepository;
    @Autowired JdbcTemplate jdbcTemplate;

    @Test
    void basePackagesFromProperties_appliedToMatchingEntity() {
        externalOrderRepository.save(new ExternalOrder(OrderStatus.CONFIRMED));

        String rawValue = jdbcTemplate.queryForObject(
            "SELECT status FROM external_orders LIMIT 1", String.class
        );

        assertEquals("CONFIRMED", rawValue,
            "Entity in the configured base package should be stored as STRING");
    }

    // When base-packages is set to com.example.external, Order (in the integration package) is
    // outside the configured scope — its enum field must not be mapped as STRING.
    @Test
    void entityOutsideConfiguredBasePackage_enumRemainsOrdinal() {
        orderRepository.save(new Order("test", OrderStatus.CONFIRMED));

        Number rawValue = jdbcTemplate.queryForObject(
            "SELECT status FROM orders LIMIT 1", Number.class
        );

        assertNotNull(rawValue);
        assertEquals(1, rawValue.intValue(),
            "Order is outside the configured base package — status must be stored as ordinal, not STRING");
    }
}
