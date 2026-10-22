package com.couturix.user.dto;

import com.couturix.domain.enums.RoleCode;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import java.util.Set;

public record CreateUserRequest(
        @Email @NotBlank String email,
        @NotBlank String password,
        @NotBlank String nom,
        @NotBlank String prenom,
        String telephone,
        @NotEmpty Set<RoleCode> roles
) {}
