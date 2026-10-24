package com.couturix.site.dto;

import java.util.List;

public record SiteSnapshotDto(
        SiteSettingsDto settings,
        List<SiteMenuDto> menus,
        List<SitePageDto> pages
) {}
