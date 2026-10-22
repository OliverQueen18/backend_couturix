package com.couturix.notification.dto;

import com.couturix.domain.enums.CanalNotification;
import com.couturix.domain.enums.EvenementNotification;
import com.couturix.domain.enums.NotificationStatut;
import java.time.Instant;

public record NotificationDto(
        Long id,
        CanalNotification canal,
        EvenementNotification evenement,
        String destinataire,
        String sujet,
        String contenu,
        NotificationStatut statut,
        String erreur,
        Instant createdAt,
        Instant sentAt
) {}
