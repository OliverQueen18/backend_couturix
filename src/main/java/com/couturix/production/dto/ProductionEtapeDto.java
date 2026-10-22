package com.couturix.production.dto;

import java.time.Instant;

public record ProductionEtapeDto(Long id, String etape, String commentaire, Instant createdAt) {}
