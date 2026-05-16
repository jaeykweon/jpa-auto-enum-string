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

    public Hibernate5EnumStringIntegrator(AutoEnumStringConfig config) {
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
                try {
                    if (applyStringMappingAlongPath(pc, desc.getPropertyPath(), desc.getFieldType())) {
                        applied.add(desc.getEntityClass().getSimpleName() + "." + String.join(".", desc.getPropertyPath()));
                    }
                } catch (Exception e) {
                    log.warning("[jpa-auto-enum-string] Could not apply STRING mapping to "
                        + desc.getEntityClass().getSimpleName() + "." + String.join(".", desc.getPropertyPath()) + ": " + e);
                }
            }
        }
        if (!applied.isEmpty()) {
            log.info("[jpa-auto-enum-string] Applied STRING mapping to " + applied.size()
                + " enum field(s): " + String.join(", ", applied));
        }
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
