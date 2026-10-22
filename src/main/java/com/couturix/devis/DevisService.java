package com.couturix.devis;
import com.couturix.client.ClientService;
import com.couturix.commande.CommandeService;
import com.couturix.common.exception.ApiException;
import com.couturix.composition.CompositionRepository;
import com.couturix.domain.enums.DevisStatut;
import com.couturix.modele.ModeleService;
import com.couturix.simulateur.SimulateurService;
import java.math.BigDecimal; import java.time.Year; import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service; import org.springframework.transaction.annotation.Transactional;
@Service @RequiredArgsConstructor
public class DevisService {
  private final DevisRepository repo;
  private final ClientService clientService;
  private final ModeleService modeleService;
  private final CompositionRepository compositionRepository;
  private final SimulateurService simulateurService;
  private final CommandeService commandeService;

  public List<Devis> list() { return repo.findAll(); }
  public Devis require(Long id) { return repo.findById(id).orElseThrow(() -> ApiException.notFound("Devis introuvable")); }

  @Transactional @PreAuthorize("hasAnyRole('SUPER_ADMIN','ADMIN','GERANT','RECEPTIONNISTE')")
  public Devis create(Devis d) {
    d.setNumero(next());
    d.setClient(clientService.require(d.getClient().getId()));
    if (d.getModele()!=null && d.getModele().getId()!=null) {
      d.setModele(modeleService.require(d.getModele().getId()));
      if (d.getDelaiJours() == null && d.getModele().getTempsFabricationJours() != null) {
        d.setDelaiJours(d.getModele().getTempsFabricationJours());
      }
    }
    BigDecimal brut = nvl(d.getPrixUnitaire()).multiply(BigDecimal.valueOf(Math.max(d.getQuantite(),1)));
    d.setMontantTotal(brut.subtract(nvl(d.getRemise())).max(BigDecimal.ZERO));
    return repo.save(d);
  }

  @Transactional
  public Devis fromSimulation(Long simulationId, Long clientId) {
    var sim = simulateurService.require(simulationId);
    Devis d = new Devis(); d.setClient(clientService.require(clientId)); d.setModele(sim.getModele()); d.setComposition(sim.getComposition());
    d.setSimulation(sim); d.setQuantite(sim.getQuantite());
    d.setPrixUnitaire(sim.getQuantite()==0?sim.getPrixVente():sim.getPrixVente().divide(BigDecimal.valueOf(sim.getQuantite()), 2, java.math.RoundingMode.HALF_UP));
    d.setRemise(sim.getRemise()); d.setMontantTotal(sim.getPrixVente().subtract(sim.getRemise()));
    return create(d);
  }

  @Transactional @PreAuthorize("hasAnyRole('SUPER_ADMIN','ADMIN','GERANT','RECEPTIONNISTE')")
  public Devis accepter(Long id) {
    Devis d = require(id);
    if (d.getStatut()==DevisStatut.COMMANDE) throw ApiException.conflict("Devis déjà transformé");
    commandeService.createFromDevis(d);
    d.setStatut(DevisStatut.COMMANDE); d.setUpdatedAt(java.time.Instant.now());
    return repo.save(d);
  }

  private String next() {
    String prefix = "DEV-" + Year.now() + "-";
    int next = repo.findMaxNumero(prefix).map(n -> Integer.parseInt(n.substring(prefix.length())) + 1).orElse(1);
    return prefix + String.format("%04d", next);
  }
  private BigDecimal nvl(BigDecimal v) { return v==null?BigDecimal.ZERO:v; }
}
