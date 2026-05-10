# jpa-auto-enum-string

Eliminates `@Enumerated(EnumType.STRING)` boilerplate from every JPA enum field.

In typical JPA applications, `EnumType.STRING` is the standard choice for enum persistence — it stores readable string values instead of fragile ordinals. But it has to be declared manually on every single enum field.

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

It scans entity classes in the configured packages, finds enum fields without an explicit `@Enumerated` annotation, and registers them as STRING type — the same result as adding `@Enumerated(EnumType.STRING)` to each field manually. 

The startup log shows which fields were applied.

## Requirements

- Java 8+
- Hibernate 5 or 6

Spring Boot 2.1+ is required only when using the Spring Boot starter. 

The library can also be used without Spring Boot — see [Manual usage](#manual-usage-without-spring-boot).

## Getting Started

### Spring Boot

Add the starter dependency:

```gradle
implementation 'io.github.jaeykweon:jpa-auto-enum-string-spring-boot-starter:1.0.0'
```

```xml
<dependency>
    <groupId>io.github.jaeykweon</groupId>
    <artifactId>jpa-auto-enum-string-spring-boot-starter</artifactId>
    <version>1.0.0</version>
</dependency>
```

No additional configuration needed. The library detects the Hibernate version automatically and scans the package of your `@SpringBootApplication` class.

To specify packages explicitly:

```yaml
jpa:
  auto-enum-string:
    base-packages: com.example.myapp
```

In a multi-module project where entities are spread across multiple modules, list all packages:

```yaml
jpa:
  auto-enum-string:
    base-packages:
      - com.example.myapp.order
      - com.example.myapp.payment
      - com.example.myapp.delivery
```

Only entity classes under the configured packages are affected. Third-party library entities are never touched.

### Without Spring Boot

Add the adapter dependency for your Hibernate version:

```gradle
// Hibernate 5
implementation 'io.github.jaeykweon:jpa-auto-enum-string-hibernate5-adapter:1.0.0'

// Hibernate 6
implementation 'io.github.jaeykweon:jpa-auto-enum-string-hibernate6-adapter:1.0.0'
```

For setup and configuration, see [Manual usage](#manual-usage-without-spring-boot).

## Opting out

Fields with an explicit `@Enumerated` annotation are always skipped — including `@Enumerated(EnumType.ORDINAL)`.

```java
@Entity
public class Order {
    private OrderStatus status;          // auto: stored as STRING

    @Enumerated(EnumType.ORDINAL)
    private LegacyStatus legacyStatus;   // explicit: stays as ORDINAL

    @Transient
    private OrderStatus tempStatus;      // transient: skipped
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

Once enum values are stored as strings in the database, removing this library causes Hibernate to fall back to `ORDINAL` — and string-stored values will no longer be readable.

If you need to remove the library, add `@Enumerated(EnumType.STRING)` explicitly to all enum fields first.

## Manual usage (without Spring Boot)

### Hibernate 5

```gradle
implementation 'io.github.jaeykweon:jpa-auto-enum-string-hibernate5-adapter:1.0.0'
```

```java
import org.hibernate.SessionFactory;
import org.hibernate.boot.registry.BootstrapServiceRegistry;
import org.hibernate.boot.registry.BootstrapServiceRegistryBuilder;
import org.hibernate.cfg.Configuration;

AutoEnumStringConfig config = AutoEnumStringConfig.builder()
    .basePackages("com.example.myapp")
    .build();

BootstrapServiceRegistry bootstrapRegistry = new BootstrapServiceRegistryBuilder()
    .applyIntegrator(new Hibernate5EnumStringIntegrator(config))
    .build();

SessionFactory sessionFactory = new Configuration()
    .addAnnotatedClass(Order.class)  // register your entity classes
    .buildSessionFactory(bootstrapRegistry);
```

### Hibernate 6

```gradle
implementation 'io.github.jaeykweon:jpa-auto-enum-string-hibernate6-adapter:1.0.0'
```

```java
import java.util.Collections;
import org.hibernate.SessionFactory;
import org.hibernate.boot.MetadataSources;
import org.hibernate.boot.registry.StandardServiceRegistry;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.hibernate.cfg.AvailableSettings;
import org.hibernate.jpa.boot.spi.IntegratorProvider;

AutoEnumStringConfig config = AutoEnumStringConfig.builder()
    .basePackages("com.example.myapp")
    .build();

StandardServiceRegistry registry = new StandardServiceRegistryBuilder()
    .applySetting(AvailableSettings.INTEGRATOR_PROVIDER,
        (IntegratorProvider) () -> Collections.singletonList(
            new Hibernate6EnumStringIntegrator(config)
        ))
    .build();

SessionFactory sessionFactory = new MetadataSources(registry)
    .addAnnotatedClass(Order.class)  // register your entity classes
    .buildMetadata()
    .buildSessionFactory();
```

## FAQ

**Why not use `AttributeConverter`?**

`AttributeConverter<MyEnum, String>` with `@Converter(autoApply = true)` requires one converter class per enum type. 

`convertToEntityAttribute(String dbData)` does not receive type information, so a single generic converter that handles all enum types is not possible with this API. 

The boilerplate moves to a different file but does not go away.

**Why not use an Annotation Processor (compile-time)?**

The standard Java Annotation Processor API (`javax.annotation.processing`) can only generate new source files — it cannot modify existing classes. 

To inject `@Enumerated(STRING)` into existing entity classes at compile time, AST manipulation is required, which means using `com.sun.tools.javac`, a JDK-internal non-public API. 

This is what Lombok does, and Lombok developers spend significant effort maintaining compatibility with each JDK version because these internal APIs change without notice.

Hibernate itself operates at runtime, so in my opinion, runtime integration is the natural fit.

**Why not use Hibernate's `hibernate.type.prefer_native_enum_types` property?**

This property does not store enums as strings (VARCHAR).

It enables the database's native ENUM column type (e.g., PostgreSQL's `CREATE TYPE` enum), which is a different storage strategy entirely. 

It is also marked as `@Incubating` (experimental) and was introduced in Hibernate 6.5. In practice, a large number of codebases still run on Hibernate 5 and versions of Hibernate 6 below 6.5. This library is designed to be as broadly usable as possible, regardless of which Hibernate version is in use.

## License

[MIT](LICENSE)
