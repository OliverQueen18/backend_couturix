package com.couturix.client.dto;

import com.couturix.commande.dto.CommandeSummaryDto;
import com.couturix.paiement.dto.PaiementDto;
import java.util.List;

public record ClientDetailDto(
        ClientDto client,
        MesureDto mesureCourante,
        List<CommandeSummaryDto> lastCommandes,
        List<PaiementDto> lastPaiements
) {}
