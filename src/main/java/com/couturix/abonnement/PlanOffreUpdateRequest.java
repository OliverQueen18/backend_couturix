package com.couturix.abonnement;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.List;

public record PlanOffreUpdateRequest(
        @NotBlank @Size(max = 80) String libelle,
        @Size(max = 80) String kicker,
        String description,
        @Size(max = 80) String cta,
        @NotNull @Min(0) Integer prixMensuel,
        @NotNull @Min(0) @Max(80) Integer remiseAnnuelle,
        @NotNull @Min(0) @Max(3650) Integer trialDays,
        @Min(1) Integer maxAteliers,
        @NotNull Boolean sms,
        @NotNull Boolean popular,
        @NotNull Boolean actif,
        List<String> features,
        List<PlanServiceGroup> services
) {}
