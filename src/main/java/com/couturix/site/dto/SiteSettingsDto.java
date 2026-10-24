package com.couturix.site.dto;

public record SiteSettingsDto(
        String brandName,
        String tagline,
        String footerTagline,
        String footerNote,
        String headerCtaLabel,
        String headerCtaPath,
        String contactEmail,
        String contactPhones,
        String contactAddress,
        String facebookUrl,
        String instagramUrl,
        String tiktokUrl,
        String linkedinUrl,
        String youtubeUrl
) {}
