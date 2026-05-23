package com.example.app;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;

// In a multi-module project, two configurations are required:
//
// 1. @EntityScan — tells Spring Boot/Hibernate to register entities from the domain module.
//    Without this, Hibernate does not know about com.example.domain.Order at all.
//
// 2. jpa.auto-enum-string.base-packages in application.yml — tells the library which packages
//    to apply STRING mapping to. Without this, the library only scans com.example.app
//    (the @SpringBootApplication package) and skips domain entities.
//
//    application.yml:
//      jpa:
//        auto-enum-string:
//          base-packages:
//            - com.example.domain
@SpringBootApplication
@EntityScan("com.example.domain")
public class MyApplication {

    public static void main(String[] args) {
        SpringApplication.run(MyApplication.class, args);
    }
}
