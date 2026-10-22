package com.couturix.simulateur;
import com.couturix.common.exception.ApiException;
import com.couturix.composition.CompositionService;
import com.couturix.cout.CostCalculationEngine;
import com.couturix.modele.ModeleService;
import com.couturix.simulateur.dto.SimulationRequest;
import java.math.BigDecimal;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service; import org.springframework.transaction.annotation.Transactional;
@Service @RequiredArgsConstructor
public class SimulateurService {
  private final SimulationRepository repo;
  private final CompositionService compositionService;
  private final ModeleService modeleService;

  public CostCalculationEngine.ProfitResult calculer(SimulationRequest req) {
    var lines = req.lignes();
    if ((lines==null || lines.isEmpty()) && req.compositionId()!=null) {
      var c = compositionService.require(req.compositionId());
      lines = c.getLignes().stream().map(l -> new CostCalculationEngine.LineInput(l.getTypeLigne(), l.getQuantite(), l.getCoutUnitaire())).toList();
    }
    if (lines==null) lines = java.util.List.of();
    int qty = Math.max(req.quantite(),1);
    var costs = CostCalculationEngine.calculate(lines, qty);
    BigDecimal prix = req.prixVente();
    if (req.margeCible()!=null) prix = CostCalculationEngine.priceFromTargetMargin(costs.coutTotal(), req.margeCible(), qty);
    if (prix==null) prix = BigDecimal.ZERO;
    return CostCalculationEngine.profitability(costs, prix, req.remise(), qty);
  }

  @Transactional
  public Simulation sauvegarder(SimulationRequest req) {
    var result = calculer(req);
    Simulation s = new Simulation();
    if (req.modeleId()!=null) s.setModele(modeleService.require(req.modeleId()));
    if (req.compositionId()!=null) s.setComposition(compositionService.require(req.compositionId()));
    s.setLibelle(req.libelle()); s.setQuantite(Math.max(req.quantite(),1));
    s.setCoutMatiere(result.costs().coutMatiere()); s.setCoutMainOeuvre(result.costs().coutMainOeuvre());
    s.setFraisIndirects(result.costs().fraisIndirects()); s.setCoutTotal(result.costs().coutTotal());
    s.setPrixRevient(result.costs().prixRevient()); s.setPrixVente(result.prixVente()); s.setRemise(result.remise());
    s.setBenefice(result.benefice()); s.setMarge(result.marge());
    return repo.save(s);
  }

  public Simulation require(Long id) { return repo.findById(id).orElseThrow(() -> ApiException.notFound("Simulation introuvable")); }
  public java.util.List<Simulation> list() { return repo.findAll(); }
}
