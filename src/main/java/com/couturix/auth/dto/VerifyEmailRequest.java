package com.couturix.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record VerifyEmailRequest(
        @Email @NotBlank String email,
        @NotBlank @Size(min = 4, max = 8) String code
) {}
