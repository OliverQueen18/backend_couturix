package com.couturix.composition.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.util.List;

public record CompositionRequest(
        @NotNull Long modeleId,
        String libelle,
        @NotEmpty @Valid List<CompositionLigneRequest> lignes
) {}
