package com.couturix.stock.dto;

import jakarta.validation.constraints.NotBlank;

public record CategorieStockRequest(@NotBlank String nom) {}
