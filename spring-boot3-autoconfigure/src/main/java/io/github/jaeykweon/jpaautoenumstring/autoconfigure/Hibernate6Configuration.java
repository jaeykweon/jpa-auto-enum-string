package io.github.jaeykweon.jpaautoenumstring.autoconfigure;

import io.github.jaeykweon.jpaautoenumstring.AutoEnumStringConfig;
import io.github.jaeykweon.jpaautoenumstring.hibernate6.Hibernate6EnumStringIntegrator;
import org.hibernate.integrator.spi.Integrator;
import org.hibernate.jpa.boot.spi.IntegratorProvider;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.orm.jpa.HibernatePropertiesCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Configuration(proxyBeanMethods = false)
@ConditionalOnClass(name = "io.github.jaeykweon.jpaautoenumstring.hibernate6.Hibernate6EnumStringIntegrator")
class Hibernate6Configuration {

    private final AutoEnumStringConfig config;

    Hibernate6Configuration(AutoEnumStringConfig config) {
        this.config = config;
    }

    @Bean
    public HibernatePropertiesCustomizer hibernate6EnumStringIntegratorCustomizer() {
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
