package com.couturix.stock.dto;

import com.couturix.domain.enums.UniteStock;
import java.math.BigDecimal;

public record ArticleStockDto(
        Long id,
        Long categorieId,
        String categorieNom,
        Long fournisseurId,
        String fournisseurNom,
        String nom,
        String sku,
        UniteStock unite,
        BigDecimal stockActuel,
        BigDecimal stockMinimum,
        BigDecimal prixAchat,
        BigDecimal prixVente,
        boolean actif,
        boolean alerte
) {}
