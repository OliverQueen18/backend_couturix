package com.couturix.caisse;

import com.couturix.domain.enums.TypeOperationCaisse;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface CaisseOperationRepository extends JpaRepository<CaisseOperation, Long> {
    List<CaisseOperation> findByCreatedAtBetween(Instant from, Instant to);

    List<CaisseOperation> findByCaisseId(Long caisseId);

    @Query("""
            SELECT COALESCE(SUM(o.montant), 0) FROM CaisseOperation o
            WHERE o.typeOperation = :type AND o.createdAt >= :from AND o.createdAt < :to
            """)
    BigDecimal sumByTypeBetween(@Param("type") TypeOperationCaisse type,
                                @Param("from") Instant from,
                                @Param("to") Instant to);
}
