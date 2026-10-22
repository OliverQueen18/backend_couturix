package com.couturix.stock.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import java.util.List;

public record InventaireBatchRequest(@NotEmpty List<@Valid InventaireRequest> lignes) {}
