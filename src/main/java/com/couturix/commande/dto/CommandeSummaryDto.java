package com.couturix.commande.dto;

import com.couturix.domain.enums.CommandeStatut;
import com.couturix.domain.enums.Priorite;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

public record CommandeSummaryDto(
        Long id,
        String numero,
        Long clientId,
        String clientNom,
        Long modeleId,
        String modeleNom,
        Long tailleurId,
        CommandeStatut statut,
        Priorite priorite,
        LocalDate datePrevue,
        LocalDate dateLivraison,
        int quantite,
        BigDecimal montantTotal,
        boolean retard,
        Instant createdAt
) {}
