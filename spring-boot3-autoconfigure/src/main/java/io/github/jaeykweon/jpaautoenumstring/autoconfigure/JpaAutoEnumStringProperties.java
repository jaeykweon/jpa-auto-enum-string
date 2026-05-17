package io.github.jaeykweon.jpaautoenumstring.autoconfigure;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@ConfigurationProperties(prefix = "jpa.auto-enum-string")
public class JpaAutoEnumStringProperties {

    private List<String> basePackages = Collections.emptyList();

    public List<String> getBasePackages() {
        return basePackages;
    }

    public void setBasePackages(List<String> basePackages) {
        this.basePackages = new ArrayList<>(basePackages);
    }
}
