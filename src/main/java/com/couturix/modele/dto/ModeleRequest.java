package com.couturix.modele.dto;
import com.fasterxml.jackson.annotation.JsonAlias;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import java.math.BigDecimal;
public record ModeleRequest(
    @NotBlank String nom,
    Long categorieId,
    String description,
    BigDecimal prixIndicatif,
    String tissuRecommande,
    @JsonAlias("tempsFabricationMinutes") @Min(0) Integer tempsFabricationJours
) {}
