package com.couturix.atelier.dto;

public record AtelierPublicDto(
        String nom,
        String slug,
        String description,
        String ville,
        String pays,
        String logoUrl,
        String telephone,
        String siteWeb,
        String facebook,
        String instagram,
        String whatsapp,
        double noteMoyenne,
        long nombreAvis,
        boolean mieuxNote
) {}
