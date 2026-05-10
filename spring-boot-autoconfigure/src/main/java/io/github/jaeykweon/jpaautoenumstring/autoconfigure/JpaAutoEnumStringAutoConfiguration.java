package io.github.jaeykweon.jpaautoenumstring.autoconfigure;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingClass;
import org.springframework.boot.autoconfigure.orm.jpa.HibernatePropertiesCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Collections;

@AutoConfiguration
@ConditionalOnClass(name = "org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean")
public class JpaAutoEnumStringAutoConfiguration {

    @Configuration(proxyBeanMethods = false)
    @ConditionalOnClass(name = {
        "org.hibernate.mapping.BasicValue",  // Hibernate 6+ only
        "io.github.jaeykweon.jpaautoenumstring.hibernate6.Hibernate6EnumStringIntegrator"
    })
    static class Hibernate6Config {

        @Bean
        public HibernatePropertiesCustomizer hibernate6EnumStringIntegratorCustomizer() {
            return props -> props.put(
                "hibernate.integrator_provider",
                (org.hibernate.jpa.boot.spi.IntegratorProvider) () -> Collections.singletonList(
                    new io.github.jaeykweon.jpaautoenumstring.hibernate6.Hibernate6EnumStringIntegrator()
                )
            );
        }
    }

    @Configuration(proxyBeanMethods = false)
    @ConditionalOnClass(name = "io.github.jaeykweon.jpaautoenumstring.hibernate5.Hibernate5EnumStringIntegrator")
    @ConditionalOnMissingClass("org.hibernate.mapping.BasicValue")  // BasicValue only in Hibernate 6+
    static class Hibernate5Config {

        @Bean
        public HibernatePropertiesCustomizer hibernate5EnumStringIntegratorCustomizer() {
            return props -> props.put(
                "hibernate.integrator_provider",
                (org.hibernate.jpa.boot.spi.IntegratorProvider) () -> Collections.singletonList(
                    new io.github.jaeykweon.jpaautoenumstring.hibernate5.Hibernate5EnumStringIntegrator()
                )
            );
        }
    }
}
