package com.couturix.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RegisterAtelierRequest(
        @NotBlank @Size(max = 180) String atelierNom,
        String rccm,
        String nif,
        String sigle,
        Double latitude,
        Double longitude,
        String adresse,
        String ville,
        String pays,
        String telephoneAtelier,
        @Email @NotBlank String email,
        @NotBlank @Size(min = 8, max = 80) String password,
        @NotBlank String nom,
        @NotBlank String prenom,
        String telephone,
        @NotBlank(message = "Choisissez un plan") String plan
) {}
