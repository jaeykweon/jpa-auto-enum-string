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
import static org.junit.jupiter.api.Assertions.assertNotNull;

// Demonstrates the default behavior when base-packages is NOT configured.
//
// AutoConfigurationPackages resolves to com.example.app (@SpringBootApplication package).
// The domain entities are in com.example.domain — outside that scope.
// The library does not apply STRING mapping to out-of-scope entities.
@DataJpaTest
@ImportAutoConfiguration(JpaAutoEnumStringAutoConfiguration.class)
@TestPropertySource(properties = "spring.jpa.hibernate.ddl-auto=create-drop")
class WithoutBasePackagesConfigTest {

    @Autowired OrderRepository orderRepository;
    @Autowired JdbcTemplate jdbcTemplate;

    @Test
    void withoutConfig_enumInDomainModule_isStoredAsOrdinal() {
        orderRepository.save(new Order("Test order", OrderStatus.CONFIRMED));

        Number rawValue = jdbcTemplate.queryForObject(
            "SELECT status FROM orders LIMIT 1", Number.class);

        assertNotNull(rawValue);
        assertEquals(1, rawValue.intValue(),
            "Without base-packages config, domain module enum is stored as ordinal (CONFIRMED = 1), not STRING");
    }
}
