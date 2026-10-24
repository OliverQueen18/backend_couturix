package com.couturix.ticket.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record TicketSignataireRequest(
        @NotBlank @Size(max = 80) String libelle,
        @Size(max = 120) String nom,
        Integer ordre,
        Boolean actif
) {}
