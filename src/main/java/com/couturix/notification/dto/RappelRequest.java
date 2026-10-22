package com.couturix.notification.dto;

import com.couturix.domain.enums.CanalNotification;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record RappelRequest(
        @NotBlank String destinataire,
        @NotNull CanalNotification canal,
        String sujet,
        @NotBlank String message
) {}
