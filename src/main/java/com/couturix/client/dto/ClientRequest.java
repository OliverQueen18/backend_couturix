package com.couturix.client.dto;

import jakarta.validation.constraints.NotBlank;

public record ClientRequest(
        @NotBlank String nom,
        @NotBlank String prenom,
        @NotBlank String telephone,
        String email,
        String adresse,
        String notes,
        Long userId
) {}
