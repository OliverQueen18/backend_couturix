package com.couturix.abonnement;

public record AbonnementDto(
        String plan,
        String libelle,
        int ateliersUtilises,
        Integer ateliersMax,
        boolean illimite,
        boolean peutCreer
) {}
