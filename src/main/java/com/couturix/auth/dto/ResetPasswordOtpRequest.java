package com.couturix.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ResetPasswordOtpRequest(
        @Email @NotBlank String email,
        @NotBlank @Size(min = 4, max = 8) String code,
        @NotBlank @Size(min = 8, max = 80) String newPassword
) {}
