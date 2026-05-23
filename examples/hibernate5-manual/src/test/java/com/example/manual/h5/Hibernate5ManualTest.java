package com.example.manual.h5;

import io.github.jaeykweon.jpaautoenumstring.AutoEnumStringConfig;
import io.github.jaeykweon.jpaautoenumstring.hibernate5.Hibernate5EnumStringIntegrator;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.boot.MetadataSources;
import org.hibernate.boot.registry.BootstrapServiceRegistry;
import org.hibernate.boot.registry.BootstrapServiceRegistryBuilder;
import org.hibernate.boot.registry.StandardServiceRegistry;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class Hibernate5ManualTest {

    private static SessionFactory sessionFactory;

    @BeforeAll
    static void setUp() {
        AutoEnumStringConfig config = AutoEnumStringConfig.builder()
            .basePackages("com.example.manual.h5")
            .build();

        BootstrapServiceRegistry bootstrapRegistry = new BootstrapServiceRegistryBuilder()
            .applyIntegrator(new Hibernate5EnumStringIntegrator(config))
            .build();

        StandardServiceRegistry serviceRegistry = new StandardServiceRegistryBuilder(bootstrapRegistry)
            .applySetting("hibernate.connection.driver_class", "org.h2.Driver")
            .applySetting("hibernate.connection.url", "jdbc:h2:mem:h5manual;DB_CLOSE_DELAY=-1")
            .applySetting("hibernate.dialect", "org.hibernate.dialect.H2Dialect")
            .applySetting("hibernate.hbm2ddl.auto", "create-drop")
            .build();

        sessionFactory = new MetadataSources(serviceRegistry)
            .addAnnotatedClass(Order.class)
            .buildMetadata()
            .buildSessionFactory();
    }

    @AfterAll
    static void tearDown() {
        if (sessionFactory != null) sessionFactory.close();
    }

    @Test
    void unannotatedEnumField_isStoredAsString() {
        try (Session session = sessionFactory.openSession()) {
            session.beginTransaction();
            session.save(new Order("Test order", OrderStatus.CONFIRMED));
            session.getTransaction().commit();
        }

        try (Session session = sessionFactory.openSession()) {
            String raw = (String) session.createNativeQuery(
                "SELECT status FROM orders LIMIT 1").getSingleResult();
            assertEquals("CONFIRMED", raw);
        }
    }
}
