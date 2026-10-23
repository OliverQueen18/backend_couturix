package com.couturix.tailleur;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SpecialiteRepository extends JpaRepository<Specialite, Long> {
    Optional<Specialite> findByLibelleIgnoreCase(String libelle);
}
