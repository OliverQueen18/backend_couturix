package com.couturix.atelier.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record AtelierRequest(
        @NotBlank @Size(max = 180) String nom,
        String description,
        String adresse,
        String ville,
        String pays,
        String telephone,
        @Email String email,
        String siteWeb,
        String rccm,
        String nif,
        String sigle,
        Double latitude,
        Double longitude,
        String devise,
        Long parentId,
        Boolean actif
) {}
