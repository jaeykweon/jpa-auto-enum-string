package io.github.jaeykweon.jpaautoenumstring.integration;

import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderWithConverterRepository extends JpaRepository<OrderWithConverter, Long> {
}
