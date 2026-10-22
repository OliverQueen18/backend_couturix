package com.couturix.tailleur.dto;

import jakarta.validation.constraints.NotBlank;

public record SpecialiteRequest(@NotBlank String libelle) {}
