package com.example.app;

import com.example.domain.Order;
import com.example.domain.OrderStatus;
import io.github.jaeykweon.jpaautoenumstring.autoconfigure.JpaAutoEnumStringAutoConfiguration;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.TestPropertySource;

import static org.junit.jupiter.api.Assertions.assertEquals;

// Demonstrates multi-module usage with explicit base-packages configuration.
//
// When entities live in a separate module (com.example.domain), configure:
//
//   jpa:
//     auto-enum-string:
//       base-packages: com.example.domain
//
// In a real Spring Boot application (not @DataJpaTest), also add @EntityScan so that
// Spring Boot registers the domain entities with Hibernate:
//
//   @SpringBootApplication
//   @EntityScan("com.example.domain")
//   class MyApplication { }
@DataJpaTest
@ImportAutoConfiguration(JpaAutoEnumStringAutoConfiguration.class)
@TestPropertySource(properties = {
    "spring.jpa.hibernate.ddl-auto=create-drop",
    "jpa.auto-enum-string.base-packages=com.example.domain"
})
class WithBasePackagesConfigTest {

    @Autowired OrderRepository orderRepository;
    @Autowired JdbcTemplate jdbcTemplate;

    @Test
    void withConfig_enumInDomainModule_isStoredAsString() {
        orderRepository.save(new Order("Test order", OrderStatus.CONFIRMED));

        String rawValue = jdbcTemplate.queryForObject(
            "SELECT status FROM orders LIMIT 1", String.class);

        assertEquals("CONFIRMED", rawValue,
            "With base-packages=com.example.domain, enum in domain module is stored as STRING");
    }
}
