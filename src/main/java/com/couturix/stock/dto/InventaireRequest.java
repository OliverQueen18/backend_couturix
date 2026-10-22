package com.couturix.stock.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

public record InventaireRequest(@NotNull Long articleId, @NotNull @DecimalMin("0") BigDecimal quantiteReelle) {}
