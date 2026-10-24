package com.couturix.site;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SiteSlideRepository extends JpaRepository<SiteSlide, Long> {
    List<SiteSlide> findByVisibleTrueOrderBySortOrderAscIdAsc();
    List<SiteSlide> findAllByOrderBySortOrderAscIdAsc();
}
