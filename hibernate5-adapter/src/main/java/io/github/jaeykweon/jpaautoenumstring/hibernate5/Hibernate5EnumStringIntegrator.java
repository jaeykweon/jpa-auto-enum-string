package io.github.jaeykweon.jpaautoenumstring.hibernate5;

import io.github.jaeykweon.jpaautoenumstring.AutoEnumStringConfig;
import io.github.jaeykweon.jpaautoenumstring.EnumFieldDescriptor;
import io.github.jaeykweon.jpaautoenumstring.EnumFieldScanner;
import org.hibernate.boot.Metadata;
import org.hibernate.engine.spi.SessionFactoryImplementor;
import org.hibernate.integrator.spi.Integrator;
import org.hibernate.mapping.PersistentClass;
import org.hibernate.mapping.Property;
import org.hibernate.mapping.SimpleValue;
import org.hibernate.service.spi.SessionFactoryServiceRegistry;

import java.sql.Types;
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
        int count = 0;
        for (PersistentClass pc : metadata.getEntityBindings()) {
            Class<?> entityClass = pc.getMappedClass();
            if (entityClass == null) continue;
            List<EnumFieldDescriptor> fields = scanner.scan(entityClass);
            for (EnumFieldDescriptor desc : fields) {
                try {
                    Property property = pc.getProperty(desc.getFieldName());
                    if (property != null && property.getValue() instanceof SimpleValue) {
                        SimpleValue simpleValue = (SimpleValue) property.getValue();
                        simpleValue.setTypeName(org.hibernate.type.EnumType.class.getName());
                        Properties params = new Properties();
                        params.setProperty(org.hibernate.type.EnumType.ENUM, desc.getFieldType().getName());
                        params.setProperty(org.hibernate.type.EnumType.TYPE, String.valueOf(Types.VARCHAR));
                        simpleValue.setTypeParameters(params);
                        count++;
                    }
                } catch (Exception e) {
                    log.warning("[jpa-auto-enum-string] Could not apply STRING mapping to "
                        + desc.getEntityClass().getSimpleName() + "." + desc.getFieldName() + ": " + e.getMessage());
                }
            }
        }
        if (count > 0) {
            log.info("[jpa-auto-enum-string] Applied STRING mapping to " + count + " enum field(s).");
        }
    }

    @Override
    public void disintegrate(SessionFactoryImplementor sessionFactory,
                             SessionFactoryServiceRegistry serviceRegistry) {
    }
}
