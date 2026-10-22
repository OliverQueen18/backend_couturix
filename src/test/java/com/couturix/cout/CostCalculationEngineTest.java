package com.couturix.cout;

import com.couturix.domain.enums.TypeLigneComposition;
import java.math.BigDecimal;
import java.util.List;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CostCalculationEngineTest {

    @Test
    void calculeCoutsBoubou() {
        List<CostCalculationEngine.LineInput> lines = List.of(
                new CostCalculationEngine.LineInput(TypeLigneComposition.MATIERE, new BigDecimal("4"), new BigDecimal("2500")),
                new CostCalculationEngine.LineInput(TypeLigneComposition.CONSOMMABLE, BigDecimal.ONE, new BigDecimal("200")),
                new CostCalculationEngine.LineInput(TypeLigneComposition.ACCESSOIRE, new BigDecimal("6"), new BigDecimal("50")),
                new CostCalculationEngine.LineInput(TypeLigneComposition.MAIN_OEUVRE, BigDecimal.ONE, new BigDecimal("4000")),
                new CostCalculationEngine.LineInput(TypeLigneComposition.EMBALLAGE, BigDecimal.ONE, new BigDecimal("300")),
                new CostCalculationEngine.LineInput(TypeLigneComposition.FRAIS_INDIRECTS, BigDecimal.ONE, new BigDecimal("500"))
        );
        var result = CostCalculationEngine.calculate(lines, 1);
        assertEquals(new BigDecimal("10000.00"), result.coutMatiere());
        assertEquals(new BigDecimal("4000.00"), result.coutMainOeuvre());
        assertEquals(new BigDecimal("15300.00"), result.coutTotal());
        assertEquals(new BigDecimal("15300.00"), result.prixRevient());
    }

    @Test
    void calculeMargeEtRemise() {
        var costs = CostCalculationEngine.calculate(List.of(
                new CostCalculationEngine.LineInput(TypeLigneComposition.MATIERE, BigDecimal.ONE, new BigDecimal("1000"))
        ), 1);
        var profit = CostCalculationEngine.profitability(costs, new BigDecimal("2000"), new BigDecimal("200"), 1);
        assertEquals(new BigDecimal("1800.00"), profit.montantNet());
        assertEquals(new BigDecimal("800.00"), profit.benefice());
        assertTrue(profit.marge().compareTo(new BigDecimal("0.44")) > 0);
    }

    @Test
    void prixSelonMargeCible() {
        BigDecimal prix = CostCalculationEngine.priceFromTargetMargin(new BigDecimal("8000"), new BigDecimal("0.20"), 1);
        assertEquals(new BigDecimal("10000.00"), prix);
    }

    @Test
    void refuseMargeInvalide() {
        assertThrows(IllegalArgumentException.class,
                () -> CostCalculationEngine.priceFromTargetMargin(BigDecimal.TEN, BigDecimal.ONE, 1));
    }
}
