package io.github.jaeykweon.jpaautoenumstring.integration;

import io.github.jaeykweon.jpaautoenumstring.autoconfigure.JpaAutoEnumStringAutoConfiguration;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.TestPropertySource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@DataJpaTest
@ImportAutoConfiguration(JpaAutoEnumStringAutoConfiguration.class)
@TestPropertySource(properties = "spring.jpa.hibernate.ddl-auto=create-drop")
class EmbeddableIntegrationTest {

    @Autowired OrderWithEmbeddedRepository repository;
    @Autowired JdbcTemplate jdbcTemplate;

    @Test
    void unannotatedEnumInEmbeddable_isSavedAsString() {
        repository.save(new OrderWithEmbedded(new ShippingInfo(OrderStatus.CONFIRMED)));

        String rawValue = jdbcTemplate.queryForObject(
            "SELECT status FROM orders_with_embedded LIMIT 1", String.class
        );

        assertEquals("CONFIRMED", rawValue, "Embedded enum should be stored as STRING, not ordinal");
    }

    @Test
    void unannotatedEnumInEmbeddable_isReadBackCorrectly() {
        repository.save(new OrderWithEmbedded(new ShippingInfo(OrderStatus.SHIPPED)));

        OrderWithEmbedded found = repository.findAll().get(0);

        assertEquals(OrderStatus.SHIPPED, found.getShippingInfo().getStatus());
    }

    // @Enumerated(ORDINAL) inside an @Embeddable is an explicit user decision — must not be overridden.
    @Test
    void explicitlyOrdinalEnumInEmbeddable_remainsOrdinal() {
        repository.save(new OrderWithEmbedded(new ShippingInfo(OrderStatus.CONFIRMED, OrderStatus.PENDING)));

        Number rawValue = jdbcTemplate.queryForObject(
            "SELECT legacy_status FROM orders_with_embedded LIMIT 1", Number.class
        );

        assertNotNull(rawValue);
        assertEquals(0, rawValue.intValue(), "PENDING is ordinal 0 — must stay as ordinal inside @Embeddable");
    }
}
