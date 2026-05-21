package io.github.jaeykweon.jpaautoenumstring.integration;

import io.github.jaeykweon.jpaautoenumstring.autoconfigure.JpaAutoEnumStringAutoConfiguration;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.TestPropertySource;

import static org.junit.jupiter.api.Assertions.assertEquals;

@DataJpaTest
@ImportAutoConfiguration(JpaAutoEnumStringAutoConfiguration.class)
@TestPropertySource(properties = "spring.jpa.hibernate.ddl-auto=create-drop")
class InheritanceIntegrationTest {

    @Autowired DerivedOrderRepository repository;
    @Autowired JdbcTemplate jdbcTemplate;

    @Test
    void unannotatedEnumInMappedSuperclass_isSavedAsString() {
        repository.save(new DerivedOrder(OrderStatus.CONFIRMED));

        String rawValue = jdbcTemplate.queryForObject(
            "SELECT status FROM derived_orders LIMIT 1", String.class
        );

        assertEquals("CONFIRMED", rawValue,
            "Enum field inherited from @MappedSuperclass should be stored as STRING");
    }

    @Test
    void unannotatedEnumInMappedSuperclass_isReadBackCorrectly() {
        repository.save(new DerivedOrder(OrderStatus.SHIPPED));

        DerivedOrder found = repository.findAll().get(0);

        assertEquals(OrderStatus.SHIPPED, found.getStatus());
    }
}
