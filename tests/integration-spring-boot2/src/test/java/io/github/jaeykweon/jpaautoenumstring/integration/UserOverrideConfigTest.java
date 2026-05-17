package io.github.jaeykweon.jpaautoenumstring.integration;

import io.github.jaeykweon.jpaautoenumstring.AutoEnumStringConfig;
import io.github.jaeykweon.jpaautoenumstring.autoconfigure.JpaAutoEnumStringAutoConfiguration;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.TestPropertySource;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

// Verifies that @ConditionalOnMissingBean works correctly:
// when the user provides their own AutoEnumStringConfig bean, the library must not create its own.
@DataJpaTest
@ImportAutoConfiguration(JpaAutoEnumStringAutoConfiguration.class)
@TestPropertySource(properties = "spring.jpa.hibernate.ddl-auto=create-drop")
@Import(UserOverrideConfigTest.CustomConfig.class)
class UserOverrideConfigTest {

    @TestConfiguration
    static class CustomConfig {
        @Bean
        public AutoEnumStringConfig autoEnumStringConfig() {
            // User restricts scanning to com.example.external only,
            // so the integration package entities must NOT be mapped as STRING.
            return AutoEnumStringConfig.builder()
                .basePackages(Collections.singletonList("com.example.external"))
                .build();
        }
    }

    @Autowired
    OrderRepository orderRepository;

    @Autowired
    JdbcTemplate jdbcTemplate;

    @Test
    void userProvidedConfig_isUsedInsteadOfAutoConfigured() {
        orderRepository.save(new Order("test", OrderStatus.CONFIRMED));

        Number rawValue = jdbcTemplate.queryForObject(
            "SELECT status FROM orders LIMIT 1", Number.class
        );

        // CONFIRMED is ordinal 1 — user's config only covers com.example.external,
        // so Order.status (in the integration package) must remain as ordinal
        assertNotNull(rawValue);
        assertEquals(1, rawValue.intValue(),
            "User-provided AutoEnumStringConfig must take precedence — library must not override it");
    }
}
