package com.couturix.atelier;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AtelierRepository extends JpaRepository<Atelier, Long> {
    Optional<Atelier> findBySlug(String slug);
    boolean existsBySlug(String slug);
    List<Atelier> findByParentIsNullOrderByNomAsc();
    List<Atelier> findByParent_IdOrderByNomAsc(Long parentId);
    List<Atelier> findByActifTrueAndParentIsNullOrderByNomAsc();
}
