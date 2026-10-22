package com.couturix.composition.dto;
import com.couturix.domain.enums.TypeLigneComposition;
import java.math.BigDecimal;
public record CompositionLigneDto(Long id, TypeLigneComposition typeLigne, Long articleId, String designation, BigDecimal quantite, BigDecimal coutUnitaire) {}
