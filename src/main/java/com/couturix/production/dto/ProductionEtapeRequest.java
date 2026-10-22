package com.couturix.production.dto;

import jakarta.validation.constraints.NotBlank;

public record ProductionEtapeRequest(@NotBlank String etape, String commentaire) {}
