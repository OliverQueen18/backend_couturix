package com.couturix.stock.dto;
import com.couturix.domain.enums.TypeMouvementStock;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
public record MouvementRequest(@NotNull Long articleId, @NotNull TypeMouvementStock type, @NotNull BigDecimal quantite, String motif, String referenceMetier) {}
