package io.github.jaeykweon.jpaautoenumstring.integration;

import io.github.jaeykweon.jpaautoenumstring.autoconfigure.JpaAutoEnumStringAutoConfiguration;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.TestPropertySource;

import static org.junit.jupiter.api.Assertions.assertEquals;

// Verifies Known Limitation: the library reads opt-out annotations from fields, not from getter methods.
// An entity using property-based access (@Id on getter) with @Enumerated(ORDINAL) on the getter
// will have STRING mapping applied by the library — the getter annotation is not seen as an opt-out.
@DataJpaTest
@ImportAutoConfiguration(JpaAutoEnumStringAutoConfiguration.class)
@TestPropertySource(properties = "spring.jpa.hibernate.ddl-auto=create-drop")
class PropertyBasedAccessTest {

    @Autowired
    PropertyAccessOrderRepository repository;

    @Autowired
    JdbcTemplate jdbcTemplate;

    @Test
    void propertyBasedAccess_enumAnnotationOnGetter_isIgnoredByLibrary_stringAppliedInstead() {
        repository.save(new PropertyAccessOrder(OrderStatus.CONFIRMED));

        // @Enumerated(ORDINAL) on the getter is not seen by the library (reads fields only).
        // The library applies STRING mapping — raw DB value is "CONFIRMED", not ordinal 1.
        String rawValue = jdbcTemplate.queryForObject(
            "SELECT status FROM property_access_orders LIMIT 1", String.class
        );

        assertEquals("CONFIRMED", rawValue,
            "Library applies STRING despite @Enumerated(ORDINAL) on getter — opt-out must be on the field");
    }
}
