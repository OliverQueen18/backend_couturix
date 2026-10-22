package com.couturix.commande.dto;

import com.couturix.client.dto.MesureDto;
import com.couturix.domain.enums.CommandeStatut;
import com.couturix.domain.enums.Priorite;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

public record CommandeDto(
        Long id,
        String numero,
        Long clientId,
        String clientNom,
        Long modeleId,
        String modeleNom,
        Long compositionId,
        Long devisId,
        Long tailleurId,
        String tailleurNom,
        CommandeStatut statut,
        Priorite priorite,
        LocalDate datePrevue,
        LocalDate dateLivraison,
        int quantite,
        BigDecimal prixUnitaire,
        BigDecimal remise,
        BigDecimal montantTotal,
        String notes,
        boolean retard,
        List<CommandeDetailDto> details,
        MesureDto mesure,
        Instant createdAt
) {}
