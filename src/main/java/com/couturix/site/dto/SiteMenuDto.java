package com.couturix.site.dto;

import com.couturix.domain.enums.SiteMenuLocation;

public record SiteMenuDto(
        Long id,
        SiteMenuLocation location,
        String label,
        String path,
        int sortOrder,
        boolean visible
) {}
