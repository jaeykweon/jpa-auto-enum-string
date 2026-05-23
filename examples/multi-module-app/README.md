# Multi-Module Example

Demonstrates how to use `jpa-auto-enum-string` when entities live in a separate Gradle module from the Spring Boot application.

## Module structure

```
examples/
├── multi-module-domain/   ← entities live here (com.example.domain)
│   └── src/main/java/com/example/domain/
│       ├── Order.java
│       └── OrderStatus.java
│
└── multi-module-app/      ← Spring Boot app lives here (com.example.app)
    ├── src/main/java/com/example/app/
    │   └── MyApplication.java
    ├── src/main/resources/
    │   └── application.yml
    └── src/test/java/com/example/app/
        ├── WithoutBasePackagesConfigTest.java
        └── WithBasePackagesConfigTest.java
```

## Why two configurations are required

When entities are in a separate module, two independent configurations are needed:

### 1. `@EntityScan` — registers entities with Hibernate

Spring Boot's auto-entity-scan only covers the package of your `@SpringBootApplication` class (`com.example.app`).
Entities in `com.example.domain` (a different module) are invisible to Hibernate unless you explicitly register them:

```java
@SpringBootApplication
@EntityScan("com.example.domain")
public class MyApplication { }
```

Without `@EntityScan`, Hibernate throws `Not a managed type: class com.example.domain.Order`.

### 2. `jpa.auto-enum-string.base-packages` — tells the library which packages to scan

The library defaults to the `@SpringBootApplication` package (`com.example.app`).
It will not apply STRING mapping to entities in `com.example.domain` unless you configure it:

```yaml
jpa:
  auto-enum-string:
    base-packages:
      - com.example.domain
```

Without `base-packages`, the library skips domain entities and enums are stored as ordinals (the Hibernate default).

These two configurations are independent: `@EntityScan` affects Hibernate's entity registry;
`base-packages` affects which packages the library scans. Both are required.

## Real application vs. `@DataJpaTest`

| | Real app (`MyApplication`) | `@DataJpaTest` |
|---|---|---|
| Entity discovery | `@EntityScan` required | `@EntityScan` still required for entities in a separate module |
| Library scan | `base-packages` in `application.yml` | `base-packages` via `@TestPropertySource` |

See [`MyApplication.java`](src/main/java/com/example/app/MyApplication.java) for a production-ready example,
and the test classes for the `@DataJpaTest` equivalents.

## Spring Boot version

This example uses Spring Boot 3 (`jakarta.persistence.*`). The multi-module pattern is identical for all versions —
only the dependency name and JPA import package differ:

| Spring Boot | Starter | Domain module JPA API |
|---|---|---|
| 2.x | `jpa-auto-enum-string-spring-boot2-starter` | `javax.persistence.*` |
| 3.x | `jpa-auto-enum-string-spring-boot3-starter` | `jakarta.persistence.*` |
| 4.x | `jpa-auto-enum-string-spring-boot4-starter` | `jakarta.persistence.*` |

## Tests

| Test class | Scenario | Expected behavior |
|---|---|---|
| `WithoutBasePackagesConfigTest` | No `base-packages` configured | Enum stored as ordinal (e.g. `1`) |
| `WithBasePackagesConfigTest` | `base-packages=com.example.domain` configured | Enum stored as string (e.g. `"CONFIRMED"`) |

Run with:

```bash
./gradlew :examples:multi-module-app:test
```
