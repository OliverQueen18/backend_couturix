package com.couturix.ticket.dto;

public record TicketSignataireDto(
        Long id,
        String libelle,
        String nom,
        int ordre,
        boolean actif
) {}
