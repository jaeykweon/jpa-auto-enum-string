package io.github.jaeykweon.jpaautoenumstring.integration;

import org.springframework.data.jpa.repository.JpaRepository;

interface OrderWithConverterRepository extends JpaRepository<OrderWithConverter, Long> {
}
