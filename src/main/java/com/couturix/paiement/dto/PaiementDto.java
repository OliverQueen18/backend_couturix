package com.couturix.paiement.dto;

import com.couturix.domain.enums.MoyenPaiement;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

public record PaiementDto(
        Long id,
        Long commandeId,
        Long clientId,
        BigDecimal montant,
        MoyenPaiement moyen,
        LocalDate datePaiement,
        String reference,
        String notes,
        Instant createdAt
) {}
