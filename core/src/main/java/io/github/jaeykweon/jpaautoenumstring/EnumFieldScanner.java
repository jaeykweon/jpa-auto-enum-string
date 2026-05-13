package io.github.jaeykweon.jpaautoenumstring;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class EnumFieldScanner {

    private final AutoEnumStringConfig config;

    public EnumFieldScanner(AutoEnumStringConfig config) {
        this.config = config;
    }

    public List<EnumFieldDescriptor> scan(Class<?> entityClass) {
        if (!config.isInBasePackage(entityClass)) {
            return Collections.emptyList();
        }
        List<EnumFieldDescriptor> result = new ArrayList<>();
        Class<?> current = entityClass;
        while (current != null && current != Object.class) {
            for (Field field : current.getDeclaredFields()) {
                if (shouldSkip(field)) continue;
                if (field.getType().isEnum()) {
                    result.add(new EnumFieldDescriptor(entityClass, field));
                }
            }
            current = current.getSuperclass();
        }
        return result;
    }

    private boolean shouldSkip(Field field) {
        int mod = field.getModifiers();
        if (Modifier.isStatic(mod) || Modifier.isTransient(mod)) return true;
        for (java.lang.annotation.Annotation ann : field.getAnnotations()) {
            String name = ann.annotationType().getName();
            if (name.equals("javax.persistence.Enumerated")
                || name.equals("jakarta.persistence.Enumerated")
                || name.equals("javax.persistence.Transient")
                || name.equals("jakarta.persistence.Transient")
                || name.equals("javax.persistence.Convert")
                || name.equals("jakarta.persistence.Convert")) {
                return true;
            }
        }
        return false;
    }
}
