package com.couturix.commande.dto;

import java.math.BigDecimal;

public record CommandeDetailDto(Long id, String designation, int quantite, BigDecimal prixUnitaire) {}
