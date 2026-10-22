# -*- coding: utf-8 -*-
from pathlib import Path
ROOT = Path(__file__).resolve().parents[1] / "src" / "main" / "java" / "com" / "couturix"

def w(rel, content):
    p = ROOT / rel
    p.parent.mkdir(parents=True, exist_ok=True)
    p.write_text(content.strip() + "\n", encoding="utf-8")

w("simulateur/Simulation.java", '''
package com.couturix.simulateur;
import com.couturix.composition.Composition;
import com.couturix.modele.Modele;
import com.couturix.user.User;
import jakarta.persistence.*;
import java.math.BigDecimal; import java.time.Instant;
import lombok.Getter; import lombok.Setter;
@Getter @Setter @Entity @Table(name="simulations")
public class Simulation {
  @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
  @ManyToOne @JoinColumn(name="modele_id") private Modele modele;
  @ManyToOne @JoinColumn(name="composition_id") private Composition composition;
  private String libelle;
  @Column(nullable=false) private int quantite = 1;
  @Column(name="cout_matiere", nullable=false, precision=14, scale=2) private BigDecimal coutMatiere = BigDecimal.ZERO;
  @Column(name="cout_main_oeuvre", nullable=false, precision=14, scale=2) private BigDecimal coutMainOeuvre = BigDecimal.ZERO;
  @Column(name="frais_indirects", nullable=false, precision=14, scale=2) private BigDecimal fraisIndirects = BigDecimal.ZERO;
  @Column(name="cout_total", nullable=false, precision=14, scale=2) private BigDecimal coutTotal = BigDecimal.ZERO;
  @Column(name="prix_revient", nullable=false, precision=14, scale=2) private BigDecimal prixRevient = BigDecimal.ZERO;
  @Column(name="prix_vente", nullable=false, precision=14, scale=2) private BigDecimal prixVente = BigDecimal.ZERO;
  @Column(nullable=false, precision=14, scale=2) private BigDecimal remise = BigDecimal.ZERO;
  @Column(nullable=false, precision=14, scale=2) private BigDecimal benefice = BigDecimal.ZERO;
  @Column(nullable=false, precision=8, scale=4) private BigDecimal marge = BigDecimal.ZERO;
  @Column(columnDefinition="TEXT") private String payload;
  @ManyToOne @JoinColumn(name="user_id") private User user;
  @Column(name="created_at", nullable=false) private Instant createdAt = Instant.now();
}
''')
w("simulateur/SimulationRepository.java", "package com.couturix.simulateur;\nimport org.springframework.data.jpa.repository.JpaRepository;\npublic interface SimulationRepository extends JpaRepository<Simulation, Long> {}")
w("simulateur/dto/SimulationRequest.java", '''
package com.couturix.simulateur.dto;
import com.couturix.cout.CostCalculationEngine;
import java.math.BigDecimal; import java.util.List;
public record SimulationRequest(Long modeleId, Long compositionId, String libelle, int quantite, BigDecimal prixVente, BigDecimal remise, BigDecimal margeCible, List<CostCalculationEngine.LineInput> lignes) {}
''')
w("simulateur/SimulateurService.java", '''
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
''')
w("simulateur/SimulateurController.java", '''
package com.couturix.simulateur;
import com.couturix.cout.CostCalculationEngine;
import com.couturix.simulateur.dto.SimulationRequest;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
@RestController @RequiredArgsConstructor
@PreAuthorize("hasAnyRole('SUPER_ADMIN','ADMIN','GERANT','COMPTABLE')")
public class SimulateurController {
  private final SimulateurService service;
  @PostMapping("/api/v1/simulateur/calculer") public CostCalculationEngine.ProfitResult calc(@RequestBody SimulationRequest req) { return service.calculer(req); }
  @PostMapping("/api/v1/simulateur/sauvegarder") public Simulation save(@RequestBody SimulationRequest req) { return service.sauvegarder(req); }
  @GetMapping("/api/v1/simulations") public List<Simulation> list() { return service.list(); }
}
''')

w("devis/Devis.java", '''
package com.couturix.devis;
import com.couturix.client.Client;
import com.couturix.composition.Composition;
import com.couturix.domain.enums.DevisStatut;
import com.couturix.modele.Modele;
import com.couturix.simulateur.Simulation;
import jakarta.persistence.*;
import java.math.BigDecimal; import java.time.Instant; import java.time.LocalDate;
import lombok.Getter; import lombok.Setter;
@Getter @Setter @Entity @Table(name="devis")
public class Devis {
  @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
  @Column(nullable=false, unique=true, length=40) private String numero;
  @ManyToOne(optional=false) @JoinColumn(name="client_id") private Client client;
  @ManyToOne @JoinColumn(name="modele_id") private Modele modele;
  @ManyToOne @JoinColumn(name="composition_id") private Composition composition;
  @ManyToOne @JoinColumn(name="simulation_id") private Simulation simulation;
  @Column(nullable=false) private int quantite = 1;
  @Column(name="prix_unitaire", nullable=false, precision=14, scale=2) private BigDecimal prixUnitaire = BigDecimal.ZERO;
  @Column(nullable=false, precision=14, scale=2) private BigDecimal remise = BigDecimal.ZERO;
  @Column(name="montant_total", nullable=false, precision=14, scale=2) private BigDecimal montantTotal = BigDecimal.ZERO;
  @Column(name="delai_jours") private Integer delaiJours;
  @Column(columnDefinition="TEXT") private String conditions;
  private LocalDate validite;
  @Enumerated(EnumType.STRING) @Column(nullable=false, length=20) private DevisStatut statut = DevisStatut.BROUILLON;
  @Column(name="created_at", nullable=false) private Instant createdAt = Instant.now();
  @Column(name="updated_at", nullable=false) private Instant updatedAt = Instant.now();
}
''')
w("devis/DevisRepository.java", "package com.couturix.devis;\nimport org.springframework.data.jpa.repository.JpaRepository;\npublic interface DevisRepository extends JpaRepository<Devis, Long> {\n  long countByNumeroStartingWith(String prefix);\n}")
w("devis/DevisService.java", '''
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
    if (d.getModele()!=null && d.getModele().getId()!=null) d.setModele(modeleService.require(d.getModele().getId()));
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
    commandeService.createFromDevis(d.getClient().getId(), d.getModele()==null?null:d.getModele().getId(),
      d.getComposition()==null?null:d.getComposition().getId(), d.getQuantite(), d.getPrixUnitaire(), d.getRemise(), d.getId());
    d.setStatut(DevisStatut.COMMANDE); d.setUpdatedAt(java.time.Instant.now());
    return repo.save(d);
  }

  private String next() {
    String prefix = "DEV-" + Year.now() + "-";
    return prefix + String.format("%04d", repo.countByNumeroStartingWith(prefix)+1);
  }
  private BigDecimal nvl(BigDecimal v) { return v==null?BigDecimal.ZERO:v; }
}
''')
w("devis/DevisController.java", '''
package com.couturix.devis;
import java.util.List; import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
@RestController @RequestMapping("/api/v1/devis") @RequiredArgsConstructor
@PreAuthorize("hasAnyRole('SUPER_ADMIN','ADMIN','GERANT','RECEPTIONNISTE')")
public class DevisController {
  private final DevisService service;
  @GetMapping public List<Devis> list() { return service.list(); }
  @PostMapping public Devis create(@RequestBody Devis d) { return service.create(d); }
  @PostMapping("/{id}/accepter") public Devis accepter(@PathVariable Long id) { return service.accepter(id); }
  @PostMapping("/depuis-simulation") public Devis fromSim(@RequestBody Map<String, Long> body) { return service.fromSimulation(body.get("simulationId"), body.get("clientId")); }
}
''')

w("simulateur/SimulateurController.java", '''
package com.couturix.simulateur;
import com.couturix.cout.CostCalculationEngine;
import com.couturix.devis.Devis;
import com.couturix.devis.DevisService;
import com.couturix.simulateur.dto.SimulationRequest;
import java.util.List; import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
@RestController @RequiredArgsConstructor
@PreAuthorize("hasAnyRole('SUPER_ADMIN','ADMIN','GERANT','COMPTABLE')")
public class SimulateurController {
  private final SimulateurService service;
  private final DevisService devisService;
  @PostMapping("/api/v1/simulateur/calculer") public CostCalculationEngine.ProfitResult calc(@RequestBody SimulationRequest req) { return service.calculer(req); }
  @PostMapping("/api/v1/simulateur/sauvegarder") public Simulation save(@RequestBody SimulationRequest req) { return service.sauvegarder(req); }
  @GetMapping("/api/v1/simulations") public List<Simulation> list() { return service.list(); }
  @PostMapping("/api/v1/simulateur/{id}/devis") public Devis toDevis(@PathVariable Long id, @RequestBody Map<String, Long> body) { return devisService.fromSimulation(id, body.get("clientId")); }
}
''')
print("sim+devis ok")
