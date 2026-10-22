package com.couturix.fournisseur.dto;

import jakarta.validation.constraints.NotBlank;

public record FournisseurRequest(
        @NotBlank String nom,
        String telephone,
        String email,
        String adresse,
        String notes,
        Boolean actif
) {}
