package com.couturix.tailleur.dto;

import com.couturix.domain.enums.ModeRemuneration;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.Set;

public record TailleurRequest(
        Long userId,
        @NotBlank String nom,
        @NotBlank String prenom,
        String telephone,
        String email,
        @NotNull ModeRemuneration modeRemuneration,
        BigDecimal salaireFixe,
        BigDecimal tauxPiece,
        BigDecimal pourcentage,
        Boolean actif,
        Set<Long> specialiteIds
) {}
