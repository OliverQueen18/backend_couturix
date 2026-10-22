package com.couturix.notification.dto;

import jakarta.validation.constraints.NotBlank;

public record NotificationTemplateUpdateRequest(String sujet, @NotBlank String corps) {}
