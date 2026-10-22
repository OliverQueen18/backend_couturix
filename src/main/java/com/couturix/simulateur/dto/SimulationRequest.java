package com.couturix.simulateur.dto;
import com.couturix.cout.CostCalculationEngine;
import java.math.BigDecimal; import java.util.List;
public record SimulationRequest(Long modeleId, Long compositionId, String libelle, int quantite, BigDecimal prixVente, BigDecimal remise, BigDecimal margeCible, List<CostCalculationEngine.LineInput> lignes) {}
