# Why the autoconfigure module compiles against Spring Boot 2.7

## Context

`spring-boot-autoconfigure/build.gradle.kts` declares:

```kotlin
compileOnly(platform("org.springframework.boot:spring-boot-dependencies:2.7.18"))
compileOnly("org.springframework.boot:spring-boot-autoconfigure")
```

This means the module is compiled against Spring Boot 2.7 classes, even though the library
supports Spring Boot 3.x at runtime.

## Why this is intentional

The only Spring Boot API this module uses is `HibernatePropertiesCustomizer`, introduced in
Spring Boot 2.1. This interface has the same signature in both 2.x and 3.x:

```java
public interface HibernatePropertiesCustomizer {
    void customize(Map<String, Object> hibernateProperties);
}
```

Because the API surface is identical, a JAR compiled against 2.7 runs correctly on a 3.x
runtime. The `tests/integration-jakarta` module verifies this: it runs the full integration
suite against Spring Boot 3.3.4 (Hibernate 6.5).

## The alternative and why we didn't take it

The alternative is to publish two separate autoconfigure JARs — one compiled against SB 2.x
and one against SB 3.x — and use classifiers or separate artifacts. This is necessary when
the API used in the module differs between the two versions (e.g., when the module imports
`javax.*` vs `jakarta.*` types directly).

This library avoids any `javax`/`jakarta` import in the autoconfigure module by design.
`EnumFieldScanner` uses annotation class name strings instead of direct annotation imports,
so a single compile target is sufficient to cover both.

## Supported Spring Boot versions

| Spring Boot | Hibernate | Status         |
|-------------|-----------|----------------|
| 2.1 – 2.6   | 5.x       | `spring.factories` registration |
| 2.7+        | 5.x / 6.x | `AutoConfiguration.imports` registration |
| 3.x         | 6.x       | Tested via `tests/integration-jakarta` |
