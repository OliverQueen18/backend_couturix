package com.couturix.site;

import com.couturix.domain.enums.SiteMenuLocation;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SiteMenuRepository extends JpaRepository<SiteMenu, Long> {
    List<SiteMenu> findByVisibleTrueOrderByLocationAscSortOrderAscIdAsc();
    List<SiteMenu> findAllByOrderByLocationAscSortOrderAscIdAsc();
    List<SiteMenu> findByLocationOrderBySortOrderAscIdAsc(SiteMenuLocation location);
}
