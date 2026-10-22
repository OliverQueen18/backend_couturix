package com.couturix.notification;

import java.util.Map;

public record NotificationContext(
        String numero,
        String client,
        String statut,
        String montant,
        String message,
        String email,
        String telephone
) {
    public Map<String, String> placeholders() {
        return Map.of(
                "{{numero}}", numero == null ? "" : numero,
                "{{client}}", client == null ? "" : client,
                "{{statut}}", statut == null ? "" : statut,
                "{{montant}}", montant == null ? "" : montant,
                "{{message}}", message == null ? "" : message
        );
    }
}
