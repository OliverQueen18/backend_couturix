package com.couturix.modele.dto;

import jakarta.validation.constraints.NotNull;

public record FavoriRequest(@NotNull Long modeleId) {}
