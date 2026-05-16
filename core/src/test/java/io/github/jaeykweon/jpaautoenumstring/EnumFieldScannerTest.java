package io.github.jaeykweon.jpaautoenumstring;

import jakarta.persistence.Convert;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Transient;
import org.hibernate.annotations.Type;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class EnumFieldScannerTest {

    @Test
    void unannotatedEnumField_isScanned() {
        EnumFieldScanner scanner = scannerWithNoPackageFilter();

        List<EnumFieldDescriptor> result = scanner.scan(EntityWithPlainEnum.class);

        assertEquals(1, result.size());
        assertEquals("status", result.get(0).getFieldName());
    }

    @Test
    void staticEnumField_isSkipped() {
        EnumFieldScanner scanner = scannerWithNoPackageFilter();

        List<EnumFieldDescriptor> result = scanner.scan(EntityWithStaticEnum.class);

        assertTrue(result.isEmpty());
    }

    @Test
    void javaTransientEnumField_isSkipped() {
        EnumFieldScanner scanner = scannerWithNoPackageFilter();

        List<EnumFieldDescriptor> result = scanner.scan(EntityWithJavaTransientEnum.class);

        assertTrue(result.isEmpty());
    }

    // Fields declared in a @MappedSuperclass superclass must also be picked up.
    @Test
    void enumFieldInSuperclass_isScanned() {
        EnumFieldScanner scanner = scannerWithNoPackageFilter();

        List<EnumFieldDescriptor> result = scanner.scan(EntityWithInheritedEnum.class);

        assertEquals(1, result.size());
        assertEquals("status", result.get(0).getFieldName());
    }

    @Test
    void enumeratedAnnotatedField_isSkipped() {
        EnumFieldScanner scanner = scannerWithNoPackageFilter();

        List<EnumFieldDescriptor> result = scanner.scan(EntityWithEnumeratedEnum.class);

        assertTrue(result.isEmpty());
    }

    @Test
    void convertAnnotatedField_isSkipped() {
        EnumFieldScanner scanner = scannerWithNoPackageFilter();

        List<EnumFieldDescriptor> result = scanner.scan(EntityWithConvertEnum.class);

        assertTrue(result.isEmpty());
    }

    @Test
    void jpaTransientAnnotatedField_isSkipped() {
        EnumFieldScanner scanner = scannerWithNoPackageFilter();

        List<EnumFieldDescriptor> result = scanner.scan(EntityWithJpaTransientEnum.class);

        assertTrue(result.isEmpty());
    }

    @Test
    void typeAnnotatedField_isSkipped() {
        EnumFieldScanner scanner = scannerWithNoPackageFilter();

        List<EnumFieldDescriptor> result = scanner.scan(EntityWithTypeEnum.class);

        assertTrue(result.isEmpty());
    }

    private EnumFieldScanner scannerWithNoPackageFilter() {
        return new EnumFieldScanner(AutoEnumStringConfig.builder().build());
    }

    // --- test entities ---

    enum Status { ACTIVE, INACTIVE }

    static class EntityWithPlainEnum {
        private Status status;
    }

    static class EntityWithStaticEnum {
        private static Status status;
    }

    static class EntityWithJavaTransientEnum {
        private transient Status status;
    }

    static class BaseEntity {
        private Status status;
    }

    static class EntityWithInheritedEnum extends BaseEntity {
    }

    static class EntityWithEnumeratedEnum {
        @Enumerated
        private Status status;
    }

    static class EntityWithConvertEnum {
        @Convert(converter = Object.class)
        private Status status;
    }

    static class EntityWithJpaTransientEnum {
        @Transient
        private Status status;
    }

    static class EntityWithTypeEnum {
        @Type("string")
        private Status status;
    }
}
