package com.couturix.composition.dto;

import com.couturix.domain.enums.TypeLigneComposition;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

public record CompositionLigneRequest(
        @NotNull TypeLigneComposition typeLigne,
        Long articleId,
        @NotBlank String designation,
        @NotNull BigDecimal quantite,
        BigDecimal coutUnitaire
) {}
