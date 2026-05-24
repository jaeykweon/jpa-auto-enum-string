package io.github.jaeykweon.jpaautoenumstring.integration;

import org.springframework.data.jpa.repository.JpaRepository;

interface OrderWithElementCollectionRepository extends JpaRepository<OrderWithElementCollection, Long> {}
