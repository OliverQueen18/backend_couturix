package com.couturix.notification.dto;

import com.couturix.domain.enums.CanalNotification;
import com.couturix.domain.enums.EvenementNotification;

public record NotificationTemplateDto(
        Long id,
        EvenementNotification evenement,
        CanalNotification canal,
        String sujet,
        String corps
) {}
