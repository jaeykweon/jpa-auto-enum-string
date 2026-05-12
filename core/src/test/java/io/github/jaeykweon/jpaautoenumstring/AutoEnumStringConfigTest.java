package io.github.jaeykweon.jpaautoenumstring;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AutoEnumStringConfigTest {

    // When no base packages are configured, the library should map all entities.
    // This is the default behavior for users who just add the dependency without any configuration.
    @Test
    void noBasePackagesConfigured_mapsAllEntities() {
        AutoEnumStringConfig config = AutoEnumStringConfig.builder().build();

        assertTrue(config.isInBasePackage(String.class));
    }

    @Test
    void entityInConfiguredPackage_isMapped() {
        AutoEnumStringConfig config = AutoEnumStringConfig.builder()
            .basePackages("com.example.myapp")
            .build();

        assertTrue(config.isInBasePackage(com.example.myapp.Dummy.class));
    }

    @Test
    void entityInSubPackage_isMapped() {
        AutoEnumStringConfig config = AutoEnumStringConfig.builder()
            .basePackages("com.example.myapp")
            .build();

        assertTrue(config.isInBasePackage(com.example.myapp.domain.Dummy.class));
    }

    // "com.example.myappservice" must not be treated as a sub-package of "com.example.myapp".
    // Without the dot boundary check, startsWith("com.example.myapp") would incorrectly match it.
    @Test
    void entityInSimilarlyNamedPackage_isNotMapped() {
        AutoEnumStringConfig config = AutoEnumStringConfig.builder()
            .basePackages("com.example.myapp")
            .build();

        assertFalse(config.isInBasePackage(com.example.myappservice.Dummy.class));
    }

    @Test
    void entityMatchingAnyOfMultipleBasePackages_isMapped() {
        AutoEnumStringConfig config = AutoEnumStringConfig.builder()
            .basePackages("com.example.unmatched", "com.example.matched")
            .build();

        assertTrue(config.isInBasePackage(com.example.matched.Dummy.class));
    }
}
