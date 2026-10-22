package com.couturix.cout;

import com.couturix.domain.enums.TypeLigneComposition;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

public final class CostCalculationEngine {

    private CostCalculationEngine() {}

    public record LineInput(TypeLigneComposition type, BigDecimal quantite, BigDecimal coutUnitaire) {}

    public record CostBreakdown(
            BigDecimal coutMatiere,
            BigDecimal coutConsommables,
            BigDecimal coutAccessoires,
            BigDecimal coutMainOeuvre,
            BigDecimal fraisIndirects,
            BigDecimal coutEmballage,
            BigDecimal coutTotal,
            BigDecimal coutUnitaire,
            BigDecimal prixRevient
    ) {}

    public record ProfitResult(
            CostBreakdown costs,
            BigDecimal prixVente,
            BigDecimal remise,
            BigDecimal montantNet,
            BigDecimal benefice,
            BigDecimal marge
    ) {}

    public static CostBreakdown calculate(List<LineInput> lines, int quantiteConfections) {
        int qty = Math.max(quantiteConfections, 1);
        BigDecimal matiere = sum(lines, TypeLigneComposition.MATIERE);
        BigDecimal conso = sum(lines, TypeLigneComposition.CONSOMMABLE);
        BigDecimal acc = sum(lines, TypeLigneComposition.ACCESSOIRE);
        BigDecimal mo = sum(lines, TypeLigneComposition.MAIN_OEUVRE);
        BigDecimal indirect = sum(lines, TypeLigneComposition.FRAIS_INDIRECTS);
        BigDecimal emballage = sum(lines, TypeLigneComposition.EMBALLAGE);
        BigDecimal total = matiere.add(conso).add(acc).add(mo).add(indirect).add(emballage);
        BigDecimal unitaire = total.divide(BigDecimal.valueOf(qty), 2, RoundingMode.HALF_UP);
        return new CostBreakdown(
                scale(matiere), scale(conso), scale(acc), scale(mo), scale(indirect), scale(emballage),
                scale(total), unitaire, unitaire
        );
    }

    public static ProfitResult profitability(CostBreakdown costs, BigDecimal prixVente, BigDecimal remise, int quantite) {
        int qty = Math.max(quantite, 1);
        BigDecimal vente = nvl(prixVente).multiply(BigDecimal.valueOf(qty));
        BigDecimal rem = nvl(remise);
        BigDecimal net = vente.subtract(rem).max(BigDecimal.ZERO);
        BigDecimal benefice = net.subtract(costs.coutTotal());
        BigDecimal marge = net.compareTo(BigDecimal.ZERO) == 0
                ? BigDecimal.ZERO
                : benefice.divide(net, 4, RoundingMode.HALF_UP);
        return new ProfitResult(costs, scale(vente), scale(rem), scale(net), scale(benefice), marge);
    }

    public static BigDecimal priceFromTargetMargin(BigDecimal coutTotal, BigDecimal targetMargin, int quantite) {
        int qty = Math.max(quantite, 1);
        BigDecimal margin = nvl(targetMargin);
        if (margin.compareTo(BigDecimal.ONE) >= 0) {
            throw new IllegalArgumentException("La marge cible doit être inférieure à 100%");
        }
        BigDecimal denom = BigDecimal.ONE.subtract(margin);
        if (denom.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Marge cible invalide");
        }
        BigDecimal venteTotale = nvl(coutTotal).divide(denom, 2, RoundingMode.HALF_UP);
        return venteTotale.divide(BigDecimal.valueOf(qty), 2, RoundingMode.HALF_UP);
    }

    private static BigDecimal sum(List<LineInput> lines, TypeLigneComposition type) {
        return lines.stream()
                .filter(l -> l.type() == type)
                .map(l -> nvl(l.quantite()).multiply(nvl(l.coutUnitaire())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private static BigDecimal nvl(BigDecimal value) {
        return value == null ? BigDecimal.ZERO : value;
    }

    private static BigDecimal scale(BigDecimal value) {
        return nvl(value).setScale(2, RoundingMode.HALF_UP);
    }
}
