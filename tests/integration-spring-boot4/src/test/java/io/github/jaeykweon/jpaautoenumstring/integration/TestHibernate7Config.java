package io.github.jaeykweon.jpaautoenumstring.integration;

import io.github.jaeykweon.jpaautoenumstring.AutoEnumStringConfig;
import io.github.jaeykweon.jpaautoenumstring.hibernate6.Hibernate6EnumStringIntegrator;
import org.hibernate.integrator.spi.Integrator;
import org.hibernate.jpa.boot.spi.IntegratorProvider;
import org.springframework.boot.hibernate.autoconfigure.HibernatePropertiesCustomizer;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Registers Hibernate6EnumStringIntegrator using Spring Boot 4's HibernatePropertiesCustomizer.
 *
 * In Spring Boot 4, HibernatePropertiesCustomizer moved to
 * org.springframework.boot.hibernate.autoconfigure — incompatible with the compiled
 * spring-boot-autoconfigure artifact (which targets Spring Boot 2/3).
 *
 * This configuration wires the adapter directly so the test validates H7 runtime
 * compatibility without depending on the spring-boot-autoconfigure module.
 */
@TestConfiguration
public class TestHibernate7Config {

    @Bean
    public AutoEnumStringConfig autoEnumStringConfig() {
        return AutoEnumStringConfig.builder()
                .basePackages("io.github.jaeykweon.jpaautoenumstring.integration")
                .build();
    }

    @Bean
    public HibernatePropertiesCustomizer hibernate7EnumStringIntegratorCustomizer(AutoEnumStringConfig config) {
        return props -> {
            IntegratorProvider ourProvider = () -> Collections.singletonList(
                    new Hibernate6EnumStringIntegrator(config)
            );
            Object existing = props.get("hibernate.integrator_provider");
            if (existing instanceof IntegratorProvider) {
                IntegratorProvider existingProvider = (IntegratorProvider) existing;
                props.put("hibernate.integrator_provider", (IntegratorProvider) () -> {
                    List<Integrator> merged = new ArrayList<>(existingProvider.getIntegrators());
                    merged.addAll(ourProvider.getIntegrators());
                    return merged;
                });
            } else {
                props.put("hibernate.integrator_provider", ourProvider);
            }
        };
    }
}
