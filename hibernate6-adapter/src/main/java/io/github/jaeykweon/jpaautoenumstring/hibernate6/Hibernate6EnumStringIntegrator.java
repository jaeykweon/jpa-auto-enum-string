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

    public Hibernate6EnumStringIntegrator(AutoEnumStringConfig config) {
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
                try {
                    Property property = pc.getProperty(desc.getFieldName());
                    if (property != null && property.getValue() instanceof BasicValue) {
                        BasicValue basicValue = (BasicValue) property.getValue();
                        applyStringMapping(basicValue);
                        applied.add(desc.getEntityClass().getSimpleName() + "." + desc.getFieldName());
                    }
                } catch (Exception e) {
                    log.warning("[jpa-auto-enum-string] Could not apply STRING mapping to "
                        + desc.getEntityClass().getSimpleName() + "." + desc.getFieldName() + ": " + e.getMessage());
                }
            }
        }
        if (!applied.isEmpty()) {
            log.info("[jpa-auto-enum-string] Applied STRING mapping to " + applied.size()
                + " enum field(s): " + String.join(", ", applied));
        }
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
