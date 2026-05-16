package io.github.jaeykweon.jpaautoenumstring;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

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
        scanClassHierarchy(entityClass, entityClass, Collections.<String>emptyList(), result, new HashSet<Class<?>>());
        return result;
    }

    private void scanClassHierarchy(Class<?> entityClass, Class<?> current, List<String> pathPrefix,
                                     List<EnumFieldDescriptor> result, Set<Class<?>> visitedEmbeddables) {
        if (current == null || current == Object.class) return;
        for (Field field : current.getDeclaredFields()) {
            scanField(entityClass, field, pathPrefix, result, visitedEmbeddables);
        }
        scanClassHierarchy(entityClass, current.getSuperclass(), pathPrefix, result, visitedEmbeddables);
    }

    private void scanField(Class<?> entityClass, Field field, List<String> pathPrefix,
                            List<EnumFieldDescriptor> result, Set<Class<?>> visitedEmbeddables) {
        if (shouldSkip(field)) return;
        if (field.getType().isEnum()) {
            List<String> path = append(pathPrefix, field.getName());
            result.add(new EnumFieldDescriptor(entityClass, field, Collections.unmodifiableList(path)));
        } else if (isEmbedded(field)) {
            Class<?> embeddableType = field.getType();
            // Guard against hypothetical circular embedded references
            if (visitedEmbeddables.add(embeddableType)) {
                List<String> newPrefix = append(pathPrefix, field.getName());
                scanClassHierarchy(entityClass, embeddableType, newPrefix, result, visitedEmbeddables);
                visitedEmbeddables.remove(embeddableType);
            }
        }
    }

    private boolean shouldSkip(Field field) {
        int mod = field.getModifiers();
        if (Modifier.isStatic(mod) || Modifier.isTransient(mod)) return true;
        for (Annotation ann : field.getAnnotations()) {
            String name = ann.annotationType().getName();
            if (name.equals("javax.persistence.Enumerated")
                || name.equals("jakarta.persistence.Enumerated")
                || name.equals("javax.persistence.Transient")
                || name.equals("jakarta.persistence.Transient")
                || name.equals("javax.persistence.Convert")
                || name.equals("jakarta.persistence.Convert")
                || name.equals("org.hibernate.annotations.Type")) {
                return true;
            }
        }
        return false;
    }

    private boolean isEmbedded(Field field) {
        for (Annotation ann : field.getAnnotations()) {
            String name = ann.annotationType().getName();
            if (name.equals("javax.persistence.Embedded")
                || name.equals("jakarta.persistence.Embedded")) {
                return true;
            }
        }
        // Also support implicit embedding when the field type itself is @Embeddable
        for (Annotation ann : field.getType().getAnnotations()) {
            String name = ann.annotationType().getName();
            if (name.equals("javax.persistence.Embeddable")
                || name.equals("jakarta.persistence.Embeddable")) {
                return true;
            }
        }
        return false;
    }

    private static List<String> append(List<String> list, String element) {
        List<String> result = new ArrayList<>(list);
        result.add(element);
        return result;
    }
}
