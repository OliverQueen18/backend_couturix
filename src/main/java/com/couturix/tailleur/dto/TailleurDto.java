package com.couturix.tailleur.dto;

import com.couturix.domain.enums.ModeRemuneration;
import java.math.BigDecimal;
import java.util.Set;

public record TailleurDto(
        Long id,
        String nom,
        String prenom,
        String telephone,
        String email,
        ModeRemuneration modeRemuneration,
        BigDecimal salaireFixe,
        BigDecimal tauxPiece,
        BigDecimal pourcentage,
        boolean actif,
        Set<String> specialites,
        Set<Long> specialiteIds
) {}
