package com.couturix.site;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SitePageRepository extends JpaRepository<SitePage, Long> {
    Optional<SitePage> findBySlug(String slug);
    boolean existsBySlug(String slug);
    List<SitePage> findAllByOrderByTitleAsc();
    List<SitePage> findByPublishedTrueOrderByTitleAsc();
}
