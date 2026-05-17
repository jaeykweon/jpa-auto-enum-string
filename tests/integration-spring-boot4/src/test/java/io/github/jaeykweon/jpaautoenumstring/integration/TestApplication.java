package io.github.jaeykweon.jpaautoenumstring.integration;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.persistence.autoconfigure.EntityScan;

@SpringBootApplication
@EntityScan(basePackages = {
    "io.github.jaeykweon.jpaautoenumstring.integration",
    "com.example.external"
})
public class TestApplication {
}
