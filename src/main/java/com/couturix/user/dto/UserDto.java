package com.couturix.user.dto;
import java.util.Set;
public record UserDto(Long id, String email, String nom, String prenom, String telephone, String photoUrl, boolean actif, Set<String> roles, Long atelierId, String atelierNom, String plan) {}
