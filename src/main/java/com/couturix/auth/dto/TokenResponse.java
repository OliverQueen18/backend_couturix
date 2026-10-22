package com.couturix.auth.dto;

import java.util.Set;

public record TokenResponse(
        String accessToken,
        String refreshToken,
        String tokenType,
        long expiresInSeconds,
        Long userId,
        String email,
        String nom,
        String prenom,
        Set<String> roles,
        boolean mustChangePassword,
        boolean emailVerified,
        Long atelierId,
        String atelierNom,
        String atelierLogoUrl
) {}
