package com.couturix.client.dto;

import java.math.BigDecimal;
import java.time.Instant;

public record ClientDto(
        Long id,
        Long userId,
        String nom,
        String prenom,
        String telephone,
        String email,
        String adresse,
        String photoUrl,
        String notes,
        BigDecimal solde,
        Instant createdAt
) {}
