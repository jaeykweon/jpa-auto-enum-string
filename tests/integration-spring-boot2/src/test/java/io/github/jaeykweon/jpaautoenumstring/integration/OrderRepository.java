package io.github.jaeykweon.jpaautoenumstring.integration;

import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderRepository extends JpaRepository<Order, Long> {
}
