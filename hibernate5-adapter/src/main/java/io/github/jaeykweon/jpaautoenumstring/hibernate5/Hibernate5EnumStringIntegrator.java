package io.github.jaeykweon.jpaautoenumstring.hibernate5;

import io.github.jaeykweon.jpaautoenumstring.AutoEnumStringConfig;
import io.github.jaeykweon.jpaautoenumstring.EnumFieldDescriptor;
import io.github.jaeykweon.jpaautoenumstring.EnumFieldScanner;
import org.hibernate.boot.Metadata;
import org.hibernate.engine.spi.SessionFactoryImplementor;
import org.hibernate.integrator.spi.Integrator;
import org.hibernate.mapping.Component;
import org.hibernate.mapping.PersistentClass;
import org.hibernate.mapping.Property;
import org.hibernate.mapping.SimpleValue;
import org.hibernate.service.spi.SessionFactoryServiceRegistry;

import java.lang.reflect.Field;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.logging.Logger;


public class Hibernate5EnumStringIntegrator implements Integrator {

    private static final Logger log = Logger.getLogger(Hibernate5EnumStringIntegrator.class.getName());
    private final EnumFieldScanner scanner;
    private final AutoEnumStringConfig config;

    public Hibernate5EnumStringIntegrator(AutoEnumStringConfig config) {
        this.config = config;
        this.scanner = new EnumFieldScanner(config);
    }

    @Override
    public void integrate(Metadata metadata, SessionFactoryImplementor sessionFactory,
                          SessionFactoryServiceRegistry serviceRegistry) {
        List<String> applied = new ArrayList<>();
        for (PersistentClass pc : metadata.getEntityBindings()) {
            Class<?> entityClass = pc.getMappedClass();
            if (entityClass == null) continue;
            List<EnumFieldDescriptor> fields = scanner.scan(entityClass);
            for (EnumFieldDescriptor desc : fields) {
                String fieldRef = desc.getEntityClass().getSimpleName() + "." + String.join(".", desc.getPropertyPath());
                try {
                    if (applyStringMappingAlongPath(pc, desc.getPropertyPath(), desc.getFieldType())) {
                        applied.add(fieldRef);
                    }
                } catch (Exception e) {
                    throw new IllegalStateException(
                        "[jpa-auto-enum-string] Failed to apply STRING mapping to " + fieldRef, e);
                }
            }
        }
        processCollectionBindings(metadata, applied);
        if (!applied.isEmpty()) {
            log.info("[jpa-auto-enum-string] Applied STRING mapping to " + applied.size()
                + " enum field(s): " + String.join(", ", applied));
        }
    }

    private void processCollectionBindings(Metadata metadata, List<String> applied) {
        for (org.hibernate.mapping.Collection collection : metadata.getCollectionBindings()) {
            String role = collection.getRole();
            int lastDot = role.lastIndexOf('.');
            if (lastDot < 0) continue;
            String className = role.substring(0, lastDot);
            String fieldName = role.substring(lastDot + 1);

            Class<?> ownerClass;
            try {
                ownerClass = Class.forName(className);
            } catch (ClassNotFoundException e) {
                continue;
            }
            if (!config.isInBasePackage(ownerClass)) continue;

            Field field;
            try {
                field = findField(ownerClass, fieldName);
            } catch (NoSuchFieldException e) {
                continue;
            }

            if (collection.getElement() instanceof Component) {
                processEmbeddableCollectionElement(
                    (Component) collection.getElement(), ownerClass, fieldName, applied);
            } else if (collection.getElement() instanceof SimpleValue) {
                SimpleValue element = (SimpleValue) collection.getElement();
                if (scanner.shouldSkip(field)) continue;
                Class<?> elementType = EnumFieldScanner.extractCollectionElementEnumType(field);
                if (elementType == null) continue;
                String fieldRef = ownerClass.getSimpleName() + "." + fieldName + "[]";
                try {
                    applyStringMappingToElement(element, elementType);
                    applied.add(fieldRef);
                } catch (Exception e) {
                    throw new IllegalStateException(
                        "[jpa-auto-enum-string] Failed to apply STRING mapping to " + fieldRef, e);
                }
            }
        }
    }

    @SuppressWarnings("unchecked")
    private void processEmbeddableCollectionElement(Component component, Class<?> ownerClass,
                                                     String fieldName, List<String> applied) {
        Class<?> embeddableClass;
        try {
            embeddableClass = component.getComponentClass();
        } catch (Exception e) {
            return;
        }
        boolean anyApplied = false;
        java.util.Iterator<Property> it = component.getPropertyIterator();
        while (it.hasNext()) {
            Property prop = it.next();
            if (!(prop.getValue() instanceof SimpleValue)) continue;
            SimpleValue sv = (SimpleValue) prop.getValue();
            Field embeddableField;
            try {
                embeddableField = findField(embeddableClass, prop.getName());
            } catch (NoSuchFieldException e) {
                continue;
            }
            if (scanner.shouldSkip(embeddableField)) continue;
            if (!embeddableField.getType().isEnum()) continue;
            String fieldRef = ownerClass.getSimpleName() + "." + fieldName + "[]." + prop.getName();
            try {
                applyStringMappingToElement(sv, embeddableField.getType());
                applied.add(fieldRef);
                anyApplied = true;
            } catch (Exception e) {
                throw new IllegalStateException(
                    "[jpa-auto-enum-string] Failed to apply STRING mapping to " + fieldRef, e);
            }
        }
        if (anyApplied) {
            try {
                Field componentTypeField = Component.class.getDeclaredField("type");
                componentTypeField.setAccessible(true);
                componentTypeField.set(component, null);
            } catch (Exception ignored) {
            }
        }
    }

    private static Field findField(Class<?> clazz, String fieldName) throws NoSuchFieldException {
        Class<?> current = clazz;
        while (current != null && current != Object.class) {
            try {
                return current.getDeclaredField(fieldName);
            } catch (NoSuchFieldException e) {
                current = current.getSuperclass();
            }
        }
        throw new NoSuchFieldException(fieldName + " in " + clazz.getName());
    }

    private static void applyStringMappingToElement(SimpleValue simpleValue, Class<?> enumType) throws Exception {
        simpleValue.setTypeName(org.hibernate.type.EnumType.class.getName());
        Properties params = new Properties();
        params.setProperty(org.hibernate.type.EnumType.ENUM, enumType.getName());
        params.setProperty(org.hibernate.type.EnumType.TYPE, String.valueOf(java.sql.Types.VARCHAR));
        simpleValue.setTypeParameters(params);

        Field simpleTypeField = SimpleValue.class.getDeclaredField("type");
        simpleTypeField.setAccessible(true);
        simpleTypeField.set(simpleValue, null);
    }

    /**
     * Navigates the property path, applies STRING mapping to the leaf SimpleValue, and clears
     * the cached ComponentType on every intermediate Component — so both DDL generation and
     * INSERT/SELECT binding pick up the new type.
     */
    private static boolean applyStringMappingAlongPath(PersistentClass pc, List<String> propertyPath,
                                                        Class<?> enumType) throws Exception {
        Property property = pc.getProperty(propertyPath.get(0));
        List<Component> components = new ArrayList<>();

        for (int i = 1; i < propertyPath.size(); i++) {
            if (!(property.getValue() instanceof Component)) return false;
            Component component = (Component) property.getValue();
            components.add(component);
            property = component.getProperty(propertyPath.get(i));
        }

        if (!(property.getValue() instanceof SimpleValue)) return false;
        SimpleValue simpleValue = (SimpleValue) property.getValue();

        simpleValue.setTypeName(org.hibernate.type.EnumType.class.getName());
        Properties params = new Properties();
        params.setProperty(org.hibernate.type.EnumType.ENUM, enumType.getName());
        params.setProperty(org.hibernate.type.EnumType.TYPE, String.valueOf(Types.VARCHAR));
        simpleValue.setTypeParameters(params);

        // Clear SimpleValue.type so DDL and read-back resolve from the new typeName above.
        Field simpleTypeField = SimpleValue.class.getDeclaredField("type");
        simpleTypeField.setAccessible(true);
        simpleTypeField.set(simpleValue, null);

        // Clear Component.type on every parent component in the path so that Hibernate rebuilds
        // the ComponentType (which holds a Type[] array) during EntityPersister construction,
        // picking up the newly mapped enum type for INSERT/SELECT binding.
        Field componentTypeField = Component.class.getDeclaredField("type");
        componentTypeField.setAccessible(true);
        for (Component component : components) {
            componentTypeField.set(component, null);
        }

        return true;
    }

    @Override
    public void disintegrate(SessionFactoryImplementor sessionFactory,
                             SessionFactoryServiceRegistry serviceRegistry) {
    }
}
