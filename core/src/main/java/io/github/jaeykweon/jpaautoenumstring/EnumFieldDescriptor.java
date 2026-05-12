package io.github.jaeykweon.jpaautoenumstring;

import java.lang.reflect.Field;

public class EnumFieldDescriptor {

    private final Class<?> entityClass;
    private final Field field;

    public EnumFieldDescriptor(Class<?> entityClass, Field field) {
        this.entityClass = entityClass;
        this.field = field;
    }

    public Class<?> getEntityClass() {
        return entityClass;
    }

    public Field getField() {
        return field;
    }

    public String getFieldName() {
        return field.getName();
    }

    public Class<?> getFieldType() {
        return field.getType();
    }
}
