package com.couturix.abonnement;

import java.util.List;

public record PlanOffreDto(
        String code,
        String libelle,
        String kicker,
        String description,
        String cta,
        int trialDays,
        Integer maxAteliers,
        boolean sms,
        int prixMensuel,
        int remiseAnnuelle,
        boolean popular,
        boolean actif,
        List<String> features,
        List<PlanServiceGroup> services
) {}
