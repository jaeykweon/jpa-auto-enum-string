package io.github.jaeykweon.jpaautoenumstring;

import java.lang.reflect.Field;
import java.util.List;

public class EnumFieldDescriptor {

    private final Class<?> entityClass;
    private final Field field;
    private final List<String> propertyPath;

    public EnumFieldDescriptor(Class<?> entityClass, Field field, List<String> propertyPath) {
        this.entityClass = entityClass;
        this.field = field;
        this.propertyPath = propertyPath;
    }

    public Class<?> getEntityClass() {
        return entityClass;
    }

    public Field getField() {
        return field;
    }

    public Class<?> getFieldType() {
        return field.getType();
    }

    /**
     * Returns the full property path from the entity root to this field.
     * A direct field returns a single-element list; an embedded field returns
     * the chain of property names (e.g. ["address", "status"]).
     */
    public List<String> getPropertyPath() {
        return propertyPath;
    }

    /** Convenience method — returns the last element of the property path. */
    public String getFieldName() {
        return propertyPath.get(propertyPath.size() - 1);
    }
}
