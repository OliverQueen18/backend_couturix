package com.couturix.user.dto;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import java.util.Set;
public record UserRequest(@Email @NotBlank String email, String password, @NotBlank String nom, @NotBlank String prenom, String telephone, Set<String> roles, boolean actif, String plan, Long atelierId) {}
