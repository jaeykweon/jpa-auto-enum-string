# Hibernate 6 / 7 Manual Setup Example

Demonstrates direct Hibernate 6 (or 7) integration without Spring Boot.

## Key setup

Register the integrator via `BootstrapServiceRegistryBuilder`, then build the `StandardServiceRegistry` from it:

```java
AutoEnumStringConfig config = AutoEnumStringConfig.builder()
    .basePackages("com.example.myapp")
    .build();

BootstrapServiceRegistry bootstrapRegistry = new BootstrapServiceRegistryBuilder()
    .applyIntegrator(new Hibernate6EnumStringIntegrator(config))
    .build();

StandardServiceRegistry serviceRegistry = new StandardServiceRegistryBuilder(bootstrapRegistry)
    .applySetting("hibernate.connection.url", "jdbc:...")
    // ... other JDBC settings
    .build();

SessionFactory sessionFactory = new MetadataSources(serviceRegistry)
    .addAnnotatedClass(Order.class)
    .buildMetadata()
    .buildSessionFactory();
```

> **Note:** The `hibernate.integrator_provider` JPA property only works through the JPA
> `EntityManagerFactory` bootstrap path (i.e., Spring Boot). For direct `SessionFactory` creation,
> use `BootstrapServiceRegistryBuilder.applyIntegrator()` as shown above.

The same `hibernate6-adapter` works with both Hibernate 6 and 7.

See [`Hibernate6ManualTest.java`](src/test/java/com/example/manual/h6/Hibernate6ManualTest.java)
for a complete working example including JDBC properties and verification.

## Run

```bash
./gradlew :examples:hibernate6-manual:test
```
