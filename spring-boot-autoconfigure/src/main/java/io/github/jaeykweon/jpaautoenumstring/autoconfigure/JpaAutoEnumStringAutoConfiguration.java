package io.github.jaeykweon.jpaautoenumstring.autoconfigure;

import io.github.jaeykweon.jpaautoenumstring.AutoEnumStringConfig;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.AutoConfigurationPackages;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingClass;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration;
import org.springframework.boot.autoconfigure.orm.jpa.HibernatePropertiesCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Collections;
import java.util.List;

@AutoConfiguration(before = HibernateJpaAutoConfiguration.class)
@ConditionalOnClass(name = "org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean")
public class JpaAutoEnumStringAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public AutoEnumStringConfig autoEnumStringConfig(BeanFactory beanFactory) {
        List<String> basePackages = AutoConfigurationPackages.has(beanFactory)
            ? AutoConfigurationPackages.get(beanFactory)
            : Collections.emptyList();
        return AutoEnumStringConfig.builder()
            .basePackages(basePackages)
            .build();
    }

    @Configuration(proxyBeanMethods = false)
    @ConditionalOnClass(name = {
        "org.hibernate.mapping.BasicValue",
        "io.github.jaeykweon.jpaautoenumstring.hibernate6.Hibernate6EnumStringIntegrator"
    })
    static class Hibernate6Config {

        @Bean
        public HibernatePropertiesCustomizer hibernate6EnumStringIntegratorCustomizer(
            AutoEnumStringConfig config) {
            return props -> props.put(
                "hibernate.integrator_provider",
                (org.hibernate.jpa.boot.spi.IntegratorProvider) () -> Collections.singletonList(
                    new io.github.jaeykweon.jpaautoenumstring.hibernate6.Hibernate6EnumStringIntegrator(config)
                )
            );
        }
    }

    @Configuration(proxyBeanMethods = false)
    @ConditionalOnClass(name = "io.github.jaeykweon.jpaautoenumstring.hibernate5.Hibernate5EnumStringIntegrator")
    @ConditionalOnMissingClass("org.hibernate.mapping.BasicValue")
    static class Hibernate5Config {

        @Bean
        public HibernatePropertiesCustomizer hibernate5EnumStringIntegratorCustomizer(
            AutoEnumStringConfig config) {
            return props -> props.put(
                "hibernate.integrator_provider",
                (org.hibernate.jpa.boot.spi.IntegratorProvider) () -> Collections.singletonList(
                    new io.github.jaeykweon.jpaautoenumstring.hibernate5.Hibernate5EnumStringIntegrator(config)
                )
            );
        }
    }
}
