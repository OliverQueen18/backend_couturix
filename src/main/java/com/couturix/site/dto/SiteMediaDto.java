package com.couturix.site.dto;

import java.time.Instant;

public record SiteMediaDto(
        Long id,
        String originalName,
        String url,
        String mime,
        long sizeBytes,
        Instant createdAt
) {}
