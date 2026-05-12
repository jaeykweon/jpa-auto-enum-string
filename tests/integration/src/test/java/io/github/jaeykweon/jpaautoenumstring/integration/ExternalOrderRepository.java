package io.github.jaeykweon.jpaautoenumstring.integration;

import com.example.external.ExternalOrder;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ExternalOrderRepository extends JpaRepository<ExternalOrder, Long> {
}
