package io.github.jaeykweon.jpaautoenumstring.integration;

import org.springframework.data.jpa.repository.JpaRepository;

interface DerivedOrderRepository extends JpaRepository<DerivedOrder, Long> {
}
