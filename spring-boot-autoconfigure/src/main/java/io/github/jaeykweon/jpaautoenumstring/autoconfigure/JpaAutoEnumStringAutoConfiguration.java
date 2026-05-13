package io.github.jaeykweon.jpaautoenumstring.autoconfigure;

import io.github.jaeykweon.jpaautoenumstring.AutoEnumStringConfig;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.AutoConfigurationPackages;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;

import java.util.Collections;
import java.util.List;

@AutoConfiguration(before = HibernateJpaAutoConfiguration.class)
@ConditionalOnClass(name = "org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean")
@EnableConfigurationProperties(JpaAutoEnumStringProperties.class)
@Import({Hibernate6Configuration.class, Hibernate5Configuration.class})
public class JpaAutoEnumStringAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public AutoEnumStringConfig autoEnumStringConfig(BeanFactory beanFactory,
                                                     JpaAutoEnumStringProperties properties) {
        List<String> basePackages;
        if (!properties.getBasePackages().isEmpty()) {
            basePackages = properties.getBasePackages();
        } else {
            basePackages = AutoConfigurationPackages.has(beanFactory)
                    ? AutoConfigurationPackages.get(beanFactory)
                    : Collections.emptyList();
        }
        return AutoEnumStringConfig.builder()
                .basePackages(basePackages)
                .build();
    }
}
