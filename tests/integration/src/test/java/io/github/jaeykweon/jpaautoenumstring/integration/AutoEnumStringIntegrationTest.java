package io.github.jaeykweon.jpaautoenumstring.integration;

import io.github.jaeykweon.jpaautoenumstring.autoconfigure.JpaAutoEnumStringAutoConfiguration;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.TestPropertySource;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@ImportAutoConfiguration(JpaAutoEnumStringAutoConfiguration.class)
@TestPropertySource(properties = "spring.jpa.hibernate.ddl-auto=create-drop")
class AutoEnumStringIntegrationTest {

    @Autowired
    OrderRepository orderRepository;

    @Autowired
    JdbcTemplate jdbcTemplate;

    @Test
    void enumFieldIsSavedAsString() {
        orderRepository.save(new Order("Test order", OrderStatus.CONFIRMED));

        String rawValue = jdbcTemplate.queryForObject(
            "SELECT status FROM orders LIMIT 1", String.class
        );

        assertEquals("CONFIRMED", rawValue, "Enum should be stored as STRING, not ordinal");
    }

    @Test
    void enumFieldIsReadBackCorrectly() {
        orderRepository.save(new Order("Another order", OrderStatus.SHIPPED));

        Order found = orderRepository.findAll().get(0);

        assertEquals(OrderStatus.SHIPPED, found.getStatus());
    }
}
