# Hibernate 5 Manual Setup Example

Demonstrates direct Hibernate 5 integration without Spring Boot.

## Key setup

Register the integrator via `BootstrapServiceRegistryBuilder`, then build the `StandardServiceRegistry` from it:

```java
AutoEnumStringConfig config = AutoEnumStringConfig.builder()
    .basePackages("com.example.myapp")
    .build();

BootstrapServiceRegistry bootstrapRegistry = new BootstrapServiceRegistryBuilder()
    .applyIntegrator(new Hibernate5EnumStringIntegrator(config))
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

See [`Hibernate5ManualTest.java`](src/test/java/com/example/manual/h5/Hibernate5ManualTest.java)
for a complete working example including JDBC properties and verification.

## Run

```bash
./gradlew :examples:hibernate5-manual:test
```
