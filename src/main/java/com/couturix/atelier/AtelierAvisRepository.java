package com.couturix.atelier;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface AtelierAvisRepository extends JpaRepository<AtelierAvis, Long> {

    List<AtelierAvis> findByAtelierIdOrderByCreatedAtDesc(Long atelierId);

    Optional<AtelierAvis> findByAtelierIdAndClientId(Long atelierId, Long clientId);

    @Query("""
            select a.atelier.id, avg(a.note), count(a)
            from AtelierAvis a
            where a.atelier.id in :ids
            group by a.atelier.id
            """)
    List<Object[]> aggregateByAtelierIds(@Param("ids") Collection<Long> ids);
}
