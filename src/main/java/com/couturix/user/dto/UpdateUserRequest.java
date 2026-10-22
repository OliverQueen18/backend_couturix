package com.couturix.user.dto;

import com.couturix.domain.enums.RoleCode;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import java.util.Set;

public record UpdateUserRequest(
        @Email @NotBlank String email,
        @NotBlank String nom,
        @NotBlank String prenom,
        String telephone,
        Set<RoleCode> roles
) {}
