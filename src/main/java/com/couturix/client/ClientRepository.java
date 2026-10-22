package com.couturix.client;

import java.time.Instant;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface ClientRepository extends JpaRepository<Client, Long>, JpaSpecificationExecutor<Client> {
    Optional<Client> findFirstByTelephone(String telephone);
    Optional<Client> findByUserId(Long userId);
    long countByCreatedAtBetween(Instant from, Instant to);
}
