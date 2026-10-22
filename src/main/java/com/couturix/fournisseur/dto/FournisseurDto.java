package com.couturix.fournisseur.dto;

public record FournisseurDto(
        Long id, String nom, String telephone, String email, String adresse, String notes, boolean actif
) {}
