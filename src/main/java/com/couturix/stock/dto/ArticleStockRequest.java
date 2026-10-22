package com.couturix.stock.dto;

import com.couturix.domain.enums.UniteStock;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

public record ArticleStockRequest(
        Long categorieId,
        Long fournisseurId,
        @NotBlank String nom,
        @NotBlank String sku,
        @NotNull UniteStock unite,
        BigDecimal stockMinimum,
        BigDecimal prixAchat,
        BigDecimal prixVente,
        Boolean actif
) {}
