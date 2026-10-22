package com.couturix.paie;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
public interface AvanceRepository extends JpaRepository<Avance, Long> {
  List<Avance> findByTailleurIdAndStatut(Long tailleurId, String statut);
}
