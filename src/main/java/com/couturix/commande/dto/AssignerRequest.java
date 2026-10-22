package com.couturix.commande.dto;

import jakarta.validation.constraints.NotNull;

public record AssignerRequest(@NotNull Long tailleurId) {}
