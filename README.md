# jpa-auto-enum-string

**Eliminates `@Enumerated(EnumType.STRING)` boilerplate from every JPA enum field.**

In typical JPA applications, `EnumType.STRING` is the standard choice for enum persistence
— it stores readable string values instead of fragile ordinals.

But it has to be declared manually on every single enum field.

This library applies it automatically, so your entities stay clean and focused.

**Without this library**

```java
@Entity
public class Order {
    @Enumerated(EnumType.STRING)
    private OrderStatus status;

    @Enumerated(EnumType.STRING)
    private PaymentMethod paymentMethod;

    @Enumerated(EnumType.STRING)
    private DeliveryType deliveryType;
}
```

**With this library**

```java
@Entity
public class Order {
    private OrderStatus status;
    private PaymentMethod paymentMethod;
    private DeliveryType deliveryType;
}
```

## How it works

At application startup, the library hooks into Hibernate's initialization process.

It scans entity classes in the configured packages, finds enum fields without an explicit `@Enumerated` annotation, and
registers them as STRING type — the same result as adding `@Enumerated(EnumType.STRING)` to each field manually.

This includes enum fields inside `@Embeddable` components and fields inherited from `@MappedSuperclass`.

On startup, the library logs which fields were applied:

```
[jpa-auto-enum-string] Applied STRING mapping to 2 enum field(s): Order.status, Order.paymentMethod
```

If a field cannot be mapped, a warning is logged and the field is skipped — the application continues to start normally. The field will retain its default Hibernate behavior (ORDINAL) rather than failing the startup.

```
WARNING [jpa-auto-enum-string] Could not apply STRING mapping to Order.status: ...
```

## Requirements

- Java 8+
- Hibernate 5.3+

Spring Boot is required only when using the Spring Boot starter:

| Starter | Spring Boot | Hibernate |
|---|---|---|
| `jpa-auto-enum-string-spring-boot2-starter` | 2.1 – 2.7 | 5.3.x |
| `jpa-auto-enum-string-spring-boot3-starter` | 3.x | 6.x |
| `jpa-auto-enum-string-spring-boot4-starter` | 4.x | 7.x |

The library can also be used without Spring Boot — see [Manual usage](#manual-usage-without-spring-boot).

New Spring Boot and Hibernate versions will be supported as they are released. If a version you need is not yet listed, please open an [issue](https://github.com/jaeykweon/jpa-auto-enum-string/issues).

Integration tests covering Hibernate 5, 6, and 7 are included in the repository.

## Getting Started

### Spring Boot

Add the starter for your Spring Boot version:

**Spring Boot 2.x**

```gradle
implementation 'io.github.jaeykweon:jpa-auto-enum-string-spring-boot2-starter:1.0.0'
```

```xml
<dependency>
    <groupId>io.github.jaeykweon</groupId>
    <artifactId>jpa-auto-enum-string-spring-boot2-starter</artifactId>
    <version>1.0.0</version>
</dependency>
```

**Spring Boot 3.x**

```gradle
implementation 'io.github.jaeykweon:jpa-auto-enum-string-spring-boot3-starter:1.0.0'
```

```xml
<dependency>
    <groupId>io.github.jaeykweon</groupId>
    <artifactId>jpa-auto-enum-string-spring-boot3-starter</artifactId>
    <version>1.0.0</version>
</dependency>
```

**Spring Boot 4.x**

```gradle
implementation 'io.github.jaeykweon:jpa-auto-enum-string-spring-boot4-starter:1.0.0'
```

```xml
<dependency>
    <groupId>io.github.jaeykweon</groupId>
    <artifactId>jpa-auto-enum-string-spring-boot4-starter</artifactId>
    <version>1.0.0</version>
</dependency>
```

No additional configuration needed. The library scans the package of your `@SpringBootApplication` class and all sub-packages automatically.

> ⚠️ **Adding to an existing project?** Read the [Warning section](#️-warning-do-not-add-to-existing-projects-without-a-data-migration) before proceeding.

`base-packages` configuration is only needed when your entities live outside the `@SpringBootApplication` package — most commonly in a separate Gradle/Maven module:

```yaml
jpa:
  auto-enum-string:
    base-packages: com.example.myapp
```

If your entities live in a separate Gradle/Maven module, see [examples/multi-module](examples/multi-module/app/) for the full setup.

Only entity classes under the configured packages are affected. Third-party library entities are never touched.

If you add the wrong starter for your Spring Boot version (e.g. the SB2 starter with Spring Boot 3), the mismatched Hibernate integrator will cause application startup to fail. Check the [Requirements](#requirements) table to confirm the correct starter for your Spring Boot version.

### Without Spring Boot

Add the adapter dependency for your Hibernate version:

```gradle
// Hibernate 5.3+
implementation 'io.github.jaeykweon:jpa-auto-enum-string-hibernate5-adapter:1.0.0'

// Hibernate 6 or 7
implementation 'io.github.jaeykweon:jpa-auto-enum-string-hibernate6-adapter:1.0.0'
```

For setup and configuration, see [Manual usage](#manual-usage-without-spring-boot).

## Opting out

The library skips a field if any of the following apply:

- `@Enumerated` is present — the declared mapping is used as-is
- `@Convert` is present — the custom converter takes precedence
- Hibernate's `@Type` is present — the custom type mapping takes precedence
- `@Transient` or Java's `transient` keyword — not a persistent field

```java
@Entity
public class Order {
    private OrderStatus status;          // auto: stored as STRING

    @Enumerated(EnumType.ORDINAL)
    private LegacyStatus legacyStatus;   // explicit: stays as ORDINAL

    @Convert(converter = StatusConverter.class)
    private OrderStatus converted;       // custom converter: not overridden

    @Transient
    private OrderStatus tempStatus;      // transient: skipped
}
```

The same rules apply inside `@Embeddable` components.

```java
@Embeddable
public class ShippingInfo {
    private OrderStatus status;          // auto: stored as STRING

    @Enumerated(EnumType.ORDINAL)
    private OrderStatus legacyStatus;    // explicit: stays as ORDINAL
}
```

## ⚠️ Warning: Do not add to existing projects without a data migration

This library changes how Hibernate reads and writes enum fields.

If your database already stores enum values as integers (`0`, `1`, `2`),
adding this library will cause mapping failures at runtime — existing records will no longer be readable.

**Safe to add without migration if:**

- Starting a new project
- All enum fields in your database are already stored as strings

**Required before adding to an existing project:**

1. Check whether any enum fields are stored as integers in the database
2. Migrate integer values to string values (`0` → `'PENDING'`, `1` → `'COMPLETED'`, ...)
3. Then add this library

**Removing the library after it has been applied is also risky.**

Once enum values are stored as strings in the database, removing this library causes Hibernate to fall back to
`ORDINAL` — and string-stored values will no longer be readable. The error looks like:

```
org.springframework.dao.DataIntegrityViolationException:
  Could not extract column from JDBC ResultSet
  [Data conversion error converting "CONFIRMED"]
```

If you need to remove the library, add `@Enumerated(EnumType.STRING)` explicitly to all enum fields first.

## Manual usage (without Spring Boot)

### Hibernate 5.3+

```gradle
implementation 'io.github.jaeykweon:jpa-auto-enum-string-hibernate5-adapter:1.0.0'
```

See [examples/hibernate5-manual](examples/hibernate5-manual/) for a complete setup example.

### Hibernate 6 / 7

```gradle
implementation 'io.github.jaeykweon:jpa-auto-enum-string-hibernate6-adapter:1.0.0'
```

See [examples/hibernate6-manual](examples/hibernate6-manual/) for a complete setup example.

## FAQ

**Does this work with Lombok?**

Yes. Lombok annotations (`@Builder`, `@NoArgsConstructor`, `@Getter`, etc.) coexist with this library without any issues.
The library reads annotations directly from fields — Lombok-generated methods are not involved.

**Does this apply to `@ElementCollection` enum fields?**

No. `@ElementCollection` fields are not entity fields and are not processed by this library.
If you need STRING mapping for enum values in an element collection, use `@Enumerated(EnumType.STRING)` explicitly on that field.

**Why not use `AttributeConverter`?**

`AttributeConverter<MyEnum, String>` with `@Converter(autoApply = true)` requires one converter class per enum type.

`convertToEntityAttribute(String dbData)` does not receive type information, so a single generic converter that handles
all enum types is not possible with this API.

The boilerplate moves to a different file but does not go away.

**Why not use an Annotation Processor (compile-time)?**

The standard Java Annotation Processor API (`javax.annotation.processing`) can only generate new source files — it
cannot modify existing classes.

To inject `@Enumerated(STRING)` into existing entity classes at compile time, AST manipulation is required, which means
using `com.sun.tools.javac`, a JDK-internal non-public API.

This is what Lombok does, and Lombok developers spend significant effort maintaining compatibility with each JDK version
because these internal APIs change without notice.

Hibernate itself operates at runtime, so in my opinion, runtime integration is the natural fit.

**Why is Hibernate 5.3 the minimum, not 5.0?**

Hibernate 5.0 and 5.1 did not include Java 8 type support in the core module — it required a separate
`hibernate-java8` dependency. More practically, Hibernate 5.0/5.1 was the era of Spring Boot 1.4/1.5,
which predates this library's Spring Boot minimum of 2.1.

Hibernate 5.3 is what Spring Boot 2.1 ships with, so it is the earliest version that can be encountered
in a supported environment.

**Why is Spring Boot 2.1 the minimum for the SB2 starter?**

The library works by registering a Hibernate `Integrator` via the `hibernate.integrator_provider` property.
In a Spring Boot application, this property must be set before the `EntityManagerFactory` is created.

Spring Boot 2.1 introduced `HibernatePropertiesCustomizer`, a callback that runs at exactly the right moment
to inject this property. Spring Boot 2.0 does not have this callback, so there is no clean way to register
the integrator through auto-configuration.

Without Spring Boot, the adapters can be used directly with any Hibernate 5.3+, 6, or 7 setup regardless of
Spring Boot version.

**Why not use Hibernate's `hibernate.type.prefer_native_enum_types` property?**

This property does not store enums as strings (VARCHAR).

It enables the database's native ENUM column type (e.g., PostgreSQL's `CREATE TYPE` enum), which is a different storage
strategy entirely.

It is also marked as `@Incubating` (experimental) and was introduced in Hibernate 6.5. In practice, a large number of
codebases still run on Hibernate 5 and versions of Hibernate 6 below 6.5. This library is designed to be as broadly
usable as possible, regardless of which Hibernate version is in use.

## Known Limitations

### Property-based access (annotations on getter methods)

The library reads opt-out annotations (`@Enumerated`, `@Convert`, `@Type`) from **fields**, not from getter methods.

If your entity uses property-based access — where `@Id` and other JPA annotations are placed on getters instead of
fields — the library may not detect those opt-out annotations and could apply STRING mapping unexpectedly.

Field-based access (the Spring Boot convention, where `@Id` is on the field) is fully supported.

Property-based access is uncommon in Spring Boot projects, so this is not currently supported. If you need it, please [open an issue](https://github.com/jaeykweon/jpa-auto-enum-string/issues).

### `@Convert` fields with Hibernate 7 + H2 in tests

When using Spring Boot 4 (Hibernate 7) with H2 as the in-memory test database, inserting into a table that has a `@Convert`-annotated enum field may fail with:

```
Check constraint invalid: "CONSTRAINT_N: ..."
```

Hibernate 7 generates a `CHECK` constraint for `@Convert` fields based on the converter's output values (e.g. `CHECK (status IN ('P','C','S','X'))`). H2 2.4.x contains a bug where it cannot evaluate this constraint form, causing the INSERT to fail.

This is an H2-side regression ([H2 issue #4302](https://github.com/h2database/h2database/issues/4302)) fixed in [H2 PR #4311](https://github.com/h2database/h2database/pull/4311) (merged November 2025, not yet released as of H2 2.4.240).

**This library is not involved** — the same error occurs without the library. `@Convert` fields are explicitly excluded from this library's processing (see [Opting out](#opting-out)).

**Workarounds until H2 ships the fix:**

- Downgrade H2 to 2.3.x in your test dependencies
- Use a real database (e.g. PostgreSQL via Testcontainers) for integration tests

## License

[MIT](LICENSE)

## Issues

Bug reports and questions are welcome via [GitHub Issues](https://github.com/jaeykweon/jpa-auto-enum-string/issues).
