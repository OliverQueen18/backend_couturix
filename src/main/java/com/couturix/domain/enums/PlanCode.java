package com.couturix.domain.enums;

public enum PlanCode {
    GRATUIT("Gratuit", 1),
    STANDARD("Standard", 1),
    PREMIUM("Premium", 5),
    GOLD("Gold", null),
    PLATEFORME("Super administrateur", null);

    private final String libelle;
    private final Integer maxAteliers;

    PlanCode(String libelle, Integer maxAteliers) {
        this.libelle = libelle;
        this.maxAteliers = maxAteliers;
    }

    public String getLibelle() {
        return libelle;
    }

    public Integer getMaxAteliers() {
        return maxAteliers;
    }

    public boolean illimite() {
        return maxAteliers == null;
    }

    public boolean peutCreer(int ateliersExistants) {
        return illimite() || ateliersExistants < maxAteliers;
    }

    /** SMS clients via SMS Gateway, à partir de l’offre Standard. */
    public boolean autoriseSms() {
        return this != GRATUIT;
    }

    public boolean offreAtelier() {
        return this != PLATEFORME;
    }

    public static PlanCode from(String raw) {
        if (raw == null || raw.isBlank()) {
            return GRATUIT;
        }
        String normalized = raw.trim().toUpperCase();
        if ("FREE".equals(normalized)) {
            return GRATUIT;
        }
        return PlanCode.valueOf(normalized);
    }
}
