package com.couturix.production;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ProductionRepository extends JpaRepository<Production, Long>, JpaSpecificationExecutor<Production> {
    Optional<Production> findByCommandeId(Long commandeId);

    List<Production> findByTailleurId(Long tailleurId);

    @Query("""
            SELECT p FROM Production p
            WHERE p.tailleur.id = :tailleurId
              AND p.statut = com.couturix.domain.enums.ProductionStatut.TERMINEE
              AND COALESCE(p.dateFin, p.createdAt) >= :from
              AND COALESCE(p.dateFin, p.createdAt) < :to
            """)
    List<Production> findValidatedInPeriod(@Param("tailleurId") Long tailleurId,
                                           @Param("from") Instant from,
                                           @Param("to") Instant to);

    @Query("""
            SELECT p.tailleur.id, p.tailleur.nom, p.tailleur.prenom, COUNT(p)
            FROM Production p
            WHERE p.tailleur IS NOT NULL AND p.createdAt >= :from AND p.createdAt < :to
            GROUP BY p.tailleur.id, p.tailleur.nom, p.tailleur.prenom
            """)
    List<Object[]> countByTailleur(@Param("from") Instant from, @Param("to") Instant to);
}
