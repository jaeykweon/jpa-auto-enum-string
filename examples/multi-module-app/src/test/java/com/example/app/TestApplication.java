package com.example.app;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;

// Simulates the app module's main class.
// @SpringBootApplication is in com.example.app — a different package from the domain entities (com.example.domain).
//
// @EntityScan is required to register entities from the domain module with Hibernate.
// Without it, Hibernate does not know about com.example.domain.Order at all.
//
// AutoConfigurationPackages still resolves to com.example.app, so jpa.auto-enum-string.base-packages
// must also be configured explicitly to tell the library to apply STRING mapping to domain entities.
@SpringBootApplication
@EntityScan("com.example.domain")
class TestApplication {
}
