package com.couturix.composition;

import com.couturix.cout.CostCalculationEngine;
import com.couturix.domain.enums.TypeLigneComposition;
import java.math.BigDecimal;
import java.util.List;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class CompositionCostTest {

    @Test
    void coutCompositionViaMoteur() {
        List<CostCalculationEngine.LineInput> lines = List.of(
                new CostCalculationEngine.LineInput(TypeLigneComposition.MATIERE, new BigDecimal("3"), new BigDecimal("2000")),
                new CostCalculationEngine.LineInput(TypeLigneComposition.MAIN_OEUVRE, BigDecimal.ONE, new BigDecimal("5000"))
        );
        var costs = CostCalculationEngine.calculate(lines, 1);
        assertEquals(new BigDecimal("6000.00"), costs.coutMatiere());
        assertEquals(new BigDecimal("11000.00"), costs.coutTotal());
    }
}
