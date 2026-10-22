package com.couturix.atelier.dto;

import java.util.List;

public record AtelierDto(
        Long id,
        Long parentId,
        String nom,
        String slug,
        String description,
        String adresse,
        String ville,
        String pays,
        String telephone,
        String email,
        String siteWeb,
        String rccm,
        String nif,
        String sigle,
        Double latitude,
        Double longitude,
        String logoUrl,
        String devise,
        boolean actif,
        List<AtelierDto> enfants
) {}
