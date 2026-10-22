package com.couturix.atelier.dto;

import java.time.Instant;

public record AtelierAvisDto(
        Long id,
        String auteur,
        int note,
        String commentaire,
        Instant createdAt,
        boolean mine
) {}
