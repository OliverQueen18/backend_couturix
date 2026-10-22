package com.couturix.commande;

import com.couturix.domain.enums.CommandeStatut;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface CommandeRepository extends JpaRepository<Commande, Long>, JpaSpecificationExecutor<Commande> {
    Optional<Commande> findByNumero(String numero);

    List<Commande> findByClientIdOrderByCreatedAtDesc(Long clientId);

    @Query("SELECT MAX(c.numero) FROM Commande c WHERE c.numero LIKE CONCAT(:prefix, '%')")
    Optional<String> findMaxNumero(@Param("prefix") String prefix);

    long countByCreatedAtBetween(Instant from, Instant to);

    @Query("""
            SELECT COUNT(c) FROM Commande c
            WHERE c.datePrevue < :today
              AND c.statut NOT IN (com.couturix.domain.enums.CommandeStatut.LIVREE, com.couturix.domain.enums.CommandeStatut.ANNULEE)
            """)
    long countRetards(@Param("today") LocalDate today);

    List<Commande> findTop5ByClientIdOrderByCreatedAtDesc(Long clientId);

    List<Commande> findByTailleurIdAndStatutIn(Long tailleurId, List<CommandeStatut> statuts);

    long countByDatePrevueBeforeAndStatutNot(LocalDate date, CommandeStatut statut);
}
