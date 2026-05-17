package io.github.jaeykweon.jpaautoenumstring.integration;

import org.springframework.data.jpa.repository.JpaRepository;

interface OrderRepository extends JpaRepository<Order, Long> {
}
