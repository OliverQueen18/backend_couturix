package com.couturix.site;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SiteMediaRepository extends JpaRepository<SiteMedia, Long> {
    boolean existsByUrl(String url);

    List<SiteMedia> findAllByOrderByCreatedAtDescIdDesc();
}
