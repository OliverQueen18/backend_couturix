package com.couturix.site.dto;

public record SiteSlideDto(
        Long id,
        String kicker,
        String title,
        String body,
        String imageUrl,
        String ctaLabel,
        String ctaPath,
        int sortOrder,
        boolean visible
) {}
