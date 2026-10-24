package com.couturix.site.dto;

import java.time.Instant;

public record SitePageDto(
        Long id,
        String slug,
        String title,
        String eyebrow,
        String lead,
        String body,
        boolean published,
        Instant updatedAt
) {}
