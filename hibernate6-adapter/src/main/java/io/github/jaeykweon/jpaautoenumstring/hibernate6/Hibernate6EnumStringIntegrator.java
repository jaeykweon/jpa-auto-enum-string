package io.github.jaeykweon.jpaautoenumstring.hibernate6;

import io.github.jaeykweon.jpaautoenumstring.AutoEnumStringConfig;
import io.github.jaeykweon.jpaautoenumstring.EnumFieldDescriptor;
import io.github.jaeykweon.jpaautoenumstring.EnumFieldScanner;
import jakarta.persistence.EnumType;
import org.hibernate.boot.Metadata;
import org.hibernate.boot.spi.BootstrapContext;
import org.hibernate.engine.spi.SessionFactoryImplementor;
import org.hibernate.integrator.spi.Integrator;
import org.hibernate.mapping.BasicValue;
import org.hibernate.mapping.Column;
import org.hibernate.mapping.Component;
import org.hibernate.mapping.PersistentClass;
import org.hibernate.mapping.Property;
import org.hibernate.service.spi.SessionFactoryServiceRegistry;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

public class Hibernate6EnumStringIntegrator implements Integrator {

    private static final Logger log = Logger.getLogger(Hibernate6EnumStringIntegrator.class.getName());
    private final EnumFieldScanner scanner;
    private final AutoEnumStringConfig config;

    public Hibernate6EnumStringIntegrator(AutoEnumStringConfig config) {
        this.config = config;
        this.scanner = new EnumFieldScanner(config);
    }

    @Override
    public void integrate(Metadata metadata, BootstrapContext bootstrapContext,
                          SessionFactoryImplementor sessionFactory) {
        List<String> applied = new ArrayList<>();
        for (PersistentClass pc : metadata.getEntityBindings()) {
            Class<?> entityClass = pc.getMappedClass();
            if (entityClass == null) continue;
            List<EnumFieldDescriptor> fields = scanner.scan(entityClass);
            for (EnumFieldDescriptor desc : fields) {
                String fieldRef = desc.getEntityClass().getSimpleName() + "." + String.join(".", desc.getPropertyPath());
                try {
                    if (applyStringMappingAlongPath(pc, desc.getPropertyPath())) {
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

            if (collection.getElement() instanceof BasicValue) {
                BasicValue element = (BasicValue) collection.getElement();
                if (scanner.shouldSkip(field)) continue;
                Class<?> elementType = EnumFieldScanner.extractCollectionElementEnumType(field);
                if (elementType == null) continue;
                String fieldRef = ownerClass.getSimpleName() + "." + fieldName + "[]";
                try {
                    applyStringMapping(element);
                    applied.add(fieldRef);
                } catch (Exception e) {
                    throw new IllegalStateException(
                        "[jpa-auto-enum-string] Failed to apply STRING mapping to " + fieldRef, e);
                }
            } else if (collection.getElement() instanceof Component) {
                processEmbeddableCollectionElement(
                    (Component) collection.getElement(), ownerClass, fieldName, applied);
            }
        }
    }

    private void processEmbeddableCollectionElement(Component component, Class<?> ownerClass,
                                                     String fieldName, List<String> applied) {
        Class<?> embeddableClass;
        try {
            embeddableClass = component.getComponentClass();
        } catch (Exception e) {
            return;
        }
        for (Property prop : component.getProperties()) {
            if (!(prop.getValue() instanceof BasicValue)) continue;
            BasicValue bv = (BasicValue) prop.getValue();
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
                applyStringMapping(bv);
                applied.add(fieldRef);
            } catch (Exception e) {
                throw new IllegalStateException(
                    "[jpa-auto-enum-string] Failed to apply STRING mapping to " + fieldRef, e);
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

    /**
     * Navigates the property path, applies STRING mapping to the leaf BasicValue, and clears
     * the cached CompositeType on every intermediate Component.
     */
    private static boolean applyStringMappingAlongPath(PersistentClass pc, List<String> propertyPath) throws Exception {
        Property property = pc.getProperty(propertyPath.get(0));
        List<Component> components = new ArrayList<>();

        for (int i = 1; i < propertyPath.size(); i++) {
            if (!(property.getValue() instanceof Component)) return false;
            Component component = (Component) property.getValue();
            components.add(component);
            property = component.getProperty(propertyPath.get(i));
        }

        if (!(property.getValue() instanceof BasicValue)) return false;
        applyStringMapping((BasicValue) property.getValue());

        // Clear Component.type (cached CompositeType) so Hibernate rebuilds it after our
        // BasicValue change — otherwise INSERT/SELECT binding still uses the stale ordinal type.
        Field componentTypeField = Component.class.getDeclaredField("type");
        componentTypeField.setAccessible(true);
        for (Component component : components) {
            componentTypeField.set(component, null);
        }

        return true;
    }

    // In H6.4, BasicValue.resolution is null when the integrator runs (lazy), so
    // setEnumerationStyle() + resolve() is enough.
    //
    // In H6.5+, resolution is eagerly computed during metadata building.
    // resolveColumn() has a null-guard on Column.sqlTypeCode, so the DDL type
    // (TINYINT) set during the initial pass is never overwritten on re-resolve.
    // We must null out both BasicValue.resolution AND Column.sqlTypeCode before
    // calling resolve() so that resolveColumn() re-derives the type as VARCHAR.
    private static void applyStringMapping(BasicValue basicValue) throws Exception {
        basicValue.setEnumerationStyle(EnumType.STRING);
        Field resolutionField = BasicValue.class.getDeclaredField("resolution");
        resolutionField.setAccessible(true);
        if (resolutionField.get(basicValue) != null) {
            if (basicValue.getColumn() instanceof Column) {
                Column column = (Column) basicValue.getColumn();
                Field sqlTypeCodeField = Column.class.getDeclaredField("sqlTypeCode");
                sqlTypeCodeField.setAccessible(true);
                sqlTypeCodeField.set(column, null);
                resolutionField.set(basicValue, null);
                basicValue.resolve();
                // The initial ordinal resolution added a range check (e.g. "between 0 and 3").
                // After re-resolving as STRING the column type is correct, but that stale
                // ordinal check remains.  getCheckConstraints() returns an unmodifiable view,
                // so we clear the underlying list via reflection.
                Field checkConstraintsField = Column.class.getDeclaredField("checkConstraints");
                checkConstraintsField.setAccessible(true);
                List<?> checkConstraints = (List<?>) checkConstraintsField.get(column);
                if (checkConstraints != null) {
                    checkConstraints.clear();
                }
            } else {
                resolutionField.set(basicValue, null);
                basicValue.resolve();
            }
        }
    }

    @Override
    public void disintegrate(SessionFactoryImplementor sessionFactory,
                             SessionFactoryServiceRegistry serviceRegistry) {
    }
}
