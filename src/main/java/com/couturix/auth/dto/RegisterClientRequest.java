package com.couturix.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RegisterClientRequest(
        @NotBlank String nom,
        @NotBlank String prenom,
        @Email @NotBlank String email,
        @NotBlank @Size(min = 8, max = 80) String password,
        @NotBlank String telephone,
        String adresse,
        Double latitude,
        Double longitude
) {}
