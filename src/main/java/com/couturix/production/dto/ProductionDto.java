package com.couturix.production.dto;

import com.couturix.domain.enums.ProductionStatut;
import java.time.Instant;
import java.util.List;

public record ProductionDto(
        Long id,
        Long commandeId,
        String commandeNumero,
        Long tailleurId,
        String tailleurNom,
        Instant dateDebut,
        Instant dateFin,
        ProductionStatut statut,
        List<ProductionEtapeDto> etapes,
        Instant createdAt
) {}
