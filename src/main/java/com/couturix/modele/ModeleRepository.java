package com.couturix.modele;
import com.couturix.domain.enums.ModeleStatut;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.repository.*;
public interface ModeleRepository extends JpaRepository<Modele, Long>, JpaSpecificationExecutor<Modele> {
  Page<Modele> findByStatut(ModeleStatut statut, Pageable pageable);
}
