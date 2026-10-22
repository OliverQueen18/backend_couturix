package com.couturix.caisse;
import com.couturix.domain.enums.CaisseStatut; import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
public interface CaisseRepository extends JpaRepository<Caisse, Long> {
  Optional<Caisse> findFirstByStatut(CaisseStatut statut);
}
