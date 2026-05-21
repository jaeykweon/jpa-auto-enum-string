package io.github.jaeykweon.jpaautoenumstring.integration;

import org.springframework.data.jpa.repository.JpaRepository;

interface OrderWithMultipleStatusesRepository extends JpaRepository<OrderWithMultipleStatuses, Long> {
}
