package com.couturix.modele.dto;

import jakarta.validation.constraints.NotBlank;

public record DemandeRequest(
        @NotBlank String nom,
        @NotBlank String prenom,
        @NotBlank String telephone,
        String email,
        Long modeleId,
        Integer quantite,
        String notes
) {}
