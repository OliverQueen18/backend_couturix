package com.couturix.tailleur.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record SpecialiteRequest(
        @NotBlank @Size(max = 120) String libelle
) {}
