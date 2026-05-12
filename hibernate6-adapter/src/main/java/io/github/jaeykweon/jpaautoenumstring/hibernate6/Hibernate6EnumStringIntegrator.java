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
import org.hibernate.mapping.PersistentClass;
import org.hibernate.mapping.Property;
import org.hibernate.service.spi.SessionFactoryServiceRegistry;

import java.util.List;
import java.util.logging.Logger;

public class Hibernate6EnumStringIntegrator implements Integrator {

    private static final Logger log = Logger.getLogger(Hibernate6EnumStringIntegrator.class.getName());
    private final EnumFieldScanner scanner;

    public Hibernate6EnumStringIntegrator(AutoEnumStringConfig config) {
        this.scanner = new EnumFieldScanner(config);
    }

    @Override
    public void integrate(Metadata metadata, BootstrapContext bootstrapContext,
                          SessionFactoryImplementor sessionFactory) {
        int count = 0;
        for (PersistentClass pc : metadata.getEntityBindings()) {
            Class<?> entityClass = pc.getMappedClass();
            if (entityClass == null) continue;
            List<EnumFieldDescriptor> fields = scanner.scan(entityClass);
            for (EnumFieldDescriptor desc : fields) {
                try {
                    Property property = pc.getProperty(desc.getFieldName());
                    if (property != null && property.getValue() instanceof BasicValue) {
                        BasicValue basicValue = (BasicValue) property.getValue();
                        if (basicValue.getEnumerationStyle() == null) {
                            basicValue.setEnumerationStyle(EnumType.STRING);
                            count++;
                        }
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
