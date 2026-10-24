package com.couturix.abonnement;

import jakarta.validation.constraints.NotNull;

public record PlanOffreUpdateRequest(@NotNull Integer trialDays) {}
