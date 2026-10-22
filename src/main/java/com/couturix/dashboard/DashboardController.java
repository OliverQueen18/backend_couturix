package com.couturix.dashboard;

import com.couturix.caisse.CaisseOperationRepository;
import com.couturix.caisse.CaisseService;
import com.couturix.client.ClientRepository;
import com.couturix.commande.CommandeRepository;
import com.couturix.common.time.QueryInstants;
import com.couturix.domain.enums.TypeOperationCaisse;
import com.couturix.paiement.PaiementRepository;
import com.couturix.production.ProductionRepository;
import com.couturix.stock.StockService;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/dashboard")
@RequiredArgsConstructor
@PreAuthorize("isAuthenticated()")
public class DashboardController {

    private final PaiementRepository paiementRepository;
    private final CommandeRepository commandeRepository;
    private final ClientRepository clientRepository;
    private final StockService stockService;
    private final CaisseService caisseService;
    private final CaisseOperationRepository operationRepository;
    private final ProductionRepository productionRepository;

    @GetMapping
    public Map<String, Object> dashboard(@RequestParam(required = false) String from,
                                         @RequestParam(required = false) String to) {
        QueryInstants.Range range = QueryInstants.parse(from, to);
        BigDecimal ca = paiementRepository.findByCreatedAtBetween(range.start(), range.endInclusive()).stream()
                .map(p -> p.getMontant() == null ? BigDecimal.ZERO : p.getMontant())
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal depenses = nvl(operationRepository.sumByTypeBetween(TypeOperationCaisse.DEPENSE, range.start(), range.endExclusive()));
        BigDecimal solde = caisseService.soldeCourant();
        List<Map<String, Object>> productionParTailleur = productionRepository.countByTailleur(range.start(), range.endExclusive()).stream()
                .map(row -> {
                    Map<String, Object> m = new LinkedHashMap<>();
                    m.put("tailleurId", row[0]);
                    m.put("nom", row[1]);
                    m.put("prenom", row[2]);
                    m.put("productions", row[3]);
                    m.put("count", row[3]);
                    return m;
                })
                .toList();
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("chiffreAffaires", ca);
        body.put("depenses", depenses);
        body.put("beneficeEstime", ca.subtract(depenses));
        body.put("commandes", commandeRepository.countByCreatedAtBetween(range.start(), range.endInclusive()));
        body.put("commandesEnRetard", commandeRepository.countRetards(LocalDate.now()));
        body.put("nouveauxClients", clientRepository.countByCreatedAtBetween(range.start(), range.endInclusive()));
        body.put("clients", clientRepository.count());
        body.put("stockFaible", stockService.alertes().size());
        body.put("soldeCaisse", solde);
        body.put("productionParTailleur", productionParTailleur);
        return body;
    }

    private static BigDecimal nvl(BigDecimal v) {
        return v == null ? BigDecimal.ZERO : v;
    }
}
