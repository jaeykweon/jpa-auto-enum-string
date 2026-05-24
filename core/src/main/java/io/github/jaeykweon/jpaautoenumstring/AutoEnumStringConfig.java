package io.github.jaeykweon.jpaautoenumstring;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class AutoEnumStringConfig {

    private final List<String> basePackages;

    private AutoEnumStringConfig(Builder builder) {
        this.basePackages = Collections.unmodifiableList(builder.basePackages);
    }

    public static Builder builder() {
        return new Builder();
    }

    public List<String> getBasePackages() {
        return basePackages;
    }

    public boolean isInBasePackage(Class<?> clazz) {
        if (basePackages.isEmpty()) return true;
        String classPackage = clazz.getPackage() != null ? clazz.getPackage().getName() : "";
        for (String base : basePackages) {
            if (classPackage.equals(base) || classPackage.startsWith(base + ".")) {
                return true;
            }
        }
        return false;
    }

    public static class Builder {
        private List<String> basePackages = Collections.emptyList();

        public Builder basePackages(String... packages) {
            this.basePackages = new ArrayList<>(Arrays.asList(packages));
            return this;
        }

        public Builder basePackages(List<String> packages) {
            this.basePackages = new ArrayList<>(packages);
            return this;
        }

        public AutoEnumStringConfig build() {
            for (String pkg : basePackages) {
                if (pkg == null || pkg.isEmpty()) {
                    throw new IllegalArgumentException("basePackages must not contain null or empty elements");
                }
            }
            return new AutoEnumStringConfig(this);
        }
    }
}
