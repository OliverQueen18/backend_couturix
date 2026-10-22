package com.couturix.commande.dto;

import com.couturix.client.dto.MesureRequest;
import com.couturix.domain.enums.Priorite;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public record CommandeRequest(
        @NotNull Long clientId,
        Long modeleId,
        Long compositionId,
        Long devisId,
        Long tailleurId,
        Priorite priorite,
        LocalDate datePrevue,
        Integer quantite,
        BigDecimal prixUnitaire,
        BigDecimal remise,
        String notes,
        List<CommandeDetailDto> details,
        MesureRequest mesure
) {}
