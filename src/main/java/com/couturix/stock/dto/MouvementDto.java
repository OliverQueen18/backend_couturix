package com.couturix.stock.dto;

import com.couturix.domain.enums.TypeMouvementStock;
import java.math.BigDecimal;
import java.time.Instant;

public record MouvementDto(
        Long id,
        Long articleId,
        String articleNom,
        TypeMouvementStock type,
        BigDecimal quantite,
        BigDecimal stockAvant,
        BigDecimal stockApres,
        String motif,
        String referenceMetier,
        Instant createdAt
) {}
