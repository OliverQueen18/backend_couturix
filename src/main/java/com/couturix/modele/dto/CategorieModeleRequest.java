package com.couturix.modele.dto;

import jakarta.validation.constraints.NotBlank;

public record CategorieModeleRequest(
        @NotBlank String nom,
        String slug,
        String description,
        String imageUrl,
        Boolean actif
) {}
