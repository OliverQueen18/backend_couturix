package com.couturix.abonnement;

public record PlanOffreDto(
        String code,
        String libelle,
        int trialDays,
        Integer maxAteliers,
        boolean sms
) {}
