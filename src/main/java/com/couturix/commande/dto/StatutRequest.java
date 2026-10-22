package com.couturix.commande.dto;

import com.couturix.domain.enums.CommandeStatut;
import jakarta.validation.constraints.NotNull;

public record StatutRequest(@NotNull CommandeStatut statut, String commentaire) {}
