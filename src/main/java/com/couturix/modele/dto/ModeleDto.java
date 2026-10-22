package com.couturix.modele.dto;
import com.couturix.domain.enums.ModeleStatut;
import java.math.BigDecimal; import java.util.List;
public record ModeleDto(Long id, Long categorieId, String categorieNom, String nom, String description, BigDecimal prixIndicatif, String tissuRecommande, Integer tempsFabricationJours, ModeleStatut statut, List<String> images) {}
