package com.couturix.tailleur;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TailleurRepository extends JpaRepository<Tailleur, Long> {
    Optional<Tailleur> findByUserId(Long userId);

    @EntityGraph(attributePaths = "specialites")
    @Override
    List<Tailleur> findAll();

    @EntityGraph(attributePaths = "specialites")
    @Override
    Optional<Tailleur> findById(Long id);
}
