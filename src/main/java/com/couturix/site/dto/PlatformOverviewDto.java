package com.couturix.site.dto;

public record PlatformOverviewDto(
        long ateliers,
        long utilisateurs,
        long comptesClients,
        long pages,
        long menus,
        long slides
) {}
