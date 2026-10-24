package com.couturix.stock.dto;

import jakarta.validation.constraints.NotBlank;
import java.math.BigDecimal;

public record ArticleStockRequest(
        Long categorieId,
        Long fournisseurId,
        @NotBlank String nom,
        @NotBlank String sku,
        @NotBlank String unite,
        BigDecimal stockMinimum,
        BigDecimal prixAchat,
        BigDecimal prixVente,
        Boolean actif
) {}
