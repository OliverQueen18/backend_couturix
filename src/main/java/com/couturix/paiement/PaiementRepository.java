package com.couturix.paiement;
import java.time.Instant; import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
public interface PaiementRepository extends JpaRepository<Paiement, Long> {
  List<Paiement> findByClientIdOrderByCreatedAtDesc(Long clientId);
  List<Paiement> findByCreatedAtBetween(Instant from, Instant to);
}
