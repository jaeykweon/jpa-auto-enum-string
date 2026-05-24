package io.github.jaeykweon.jpaautoenumstring.integration;

import org.springframework.data.jpa.repository.JpaRepository;

interface OrderWithEmbeddableCollectionRepository extends JpaRepository<OrderWithEmbeddableCollection, Long> {}
