package io.github.jaeykweon.jpaautoenumstring;

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
}
