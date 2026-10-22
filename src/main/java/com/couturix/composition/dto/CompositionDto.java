package com.couturix.composition.dto;
import java.util.List;
public record CompositionDto(Long id, Long modeleId, int version, String libelle, boolean active, List<CompositionLigneDto> lignes) {}
