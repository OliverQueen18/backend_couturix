package com.couturix.abonnement;

import java.time.Instant;

public record EffectivePlan(
        String code,
        String libelle,
        Integer maxAteliers,
        boolean sms,
        boolean essai,
        int essaiJours,
        Integer essaiJoursRestants,
        Instant essaiFin
) {
    public boolean illimite() {
        return maxAteliers == null;
    }

    public boolean peutCreer(int ateliersExistants) {
        return illimite() || ateliersExistants < maxAteliers;
    }
}
