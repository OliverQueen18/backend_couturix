package com.couturix.abonnement;

import java.time.Instant;

public record AbonnementDto(
        String plan,
        String libelle,
        int ateliersUtilises,
        Integer ateliersMax,
        boolean illimite,
        boolean peutCreer,
        boolean sms,
        boolean smsGatewayReady,
        boolean essai,
        int essaiJours,
        Integer essaiJoursRestants,
        Instant essaiFin
) {}
