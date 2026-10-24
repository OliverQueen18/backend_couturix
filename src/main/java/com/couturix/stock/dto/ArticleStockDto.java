package com.couturix.stock.dto;

import java.math.BigDecimal;

public record ArticleStockDto(
        Long id,
        Long categorieId,
        String categorieNom,
        Long fournisseurId,
        String fournisseurNom,
        String nom,
        String sku,
        String unite,
        BigDecimal stockActuel,
        BigDecimal stockMinimum,
        BigDecimal prixAchat,
        BigDecimal prixVente,
        boolean actif,
        boolean alerte
) {}
