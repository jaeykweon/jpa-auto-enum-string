package io.github.jaeykweon.jpaautoenumstring.integration;

import org.springframework.data.jpa.repository.JpaRepository;

interface OrderWithEmbeddedRepository extends JpaRepository<OrderWithEmbedded, Long> {
}
