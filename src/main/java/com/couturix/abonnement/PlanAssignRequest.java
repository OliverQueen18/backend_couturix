package com.couturix.abonnement;

import jakarta.validation.constraints.NotBlank;

public record PlanAssignRequest(@NotBlank String plan) {}
