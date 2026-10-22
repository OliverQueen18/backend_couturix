package com.couturix.devis;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface DevisRepository extends JpaRepository<Devis, Long>, JpaSpecificationExecutor<Devis> {
    @Query("SELECT MAX(d.numero) FROM Devis d WHERE d.numero LIKE CONCAT(:prefix, '%')")
    Optional<String> findMaxNumero(@Param("prefix") String prefix);

    long countByNumeroStartingWith(String prefix);
}
