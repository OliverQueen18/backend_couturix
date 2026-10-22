# -*- coding: utf-8 -*-
from pathlib import Path
ROOT = Path(__file__).resolve().parents[1] / "src" / "main" / "java" / "com" / "couturix"

def w(rel, content):
    p = ROOT / rel
    p.parent.mkdir(parents=True, exist_ok=True)
    p.write_text(content.strip() + "\n", encoding="utf-8")

w("commande/CommandeService.java", r'''
package com.couturix.commande;
import com.couturix.client.ClientService;
import com.couturix.common.api.PageResponse;
import com.couturix.common.audit.AuditService;
import com.couturix.common.exception.ApiException;
import com.couturix.composition.CompositionRepository;
import com.couturix.domain.enums.CommandeStatut;
import com.couturix.domain.enums.EvenementNotification;
import com.couturix.domain.enums.Priorite;
import com.couturix.commande.dto.CommandeDto;
import com.couturix.commande.dto.CommandeRequest;
import com.couturix.modele.Modele;
import com.couturix.modele.ModeleService;
import com.couturix.notification.NotificationService;
import com.couturix.production.ProductionService;
import com.couturix.tailleur.TailleurService;
import java.math.BigDecimal; import java.time.LocalDate; import java.time.Year;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service; import org.springframework.transaction.annotation.Transactional;

@Service @RequiredArgsConstructor
public class CommandeService {
  private final CommandeRepository commandeRepository;
  private final CommandeStatutHistoriqueRepository historiqueRepository;
  private final ClientService clientService;
  private final ModeleService modeleService;
  private final TailleurService tailleurService;
  private final CompositionRepository compositionRepository;
  private final ProductionService productionService;
  private final NotificationService notificationService;
  private final AuditService auditService;

  @Transactional(readOnly=true)
  public PageResponse<CommandeDto> list(CommandeStatut statut, Long clientId, Pageable pageable) {
    Specification<Commande> spec = (root, q, cb) -> {
      var p = cb.conjunction();
      if (statut != null) p = cb.and(p, cb.equal(root.get("statut"), statut));
      if (clientId != null) p = cb.and(p, cb.equal(root.get("client").get("id"), clientId));
      return p;
    };
    return PageResponse.of(commandeRepository.findAll(spec, pageable).map(this::toDto));
  }

  public CommandeDto get(Long id) { return toDto(require(id)); }
  public Commande require(Long id) { return commandeRepository.findById(id).orElseThrow(() -> ApiException.notFound("Commande introuvable")); }

  @Transactional @PreAuthorize("hasAnyRole('SUPER_ADMIN','ADMIN','GERANT','RECEPTIONNISTE')")
  public CommandeDto create(CommandeRequest req) { return toDto(build(req, CommandeStatut.BROUILLON, null)); }

  @Transactional
  public CommandeDto createFromPublic(Long clientId, Long modeleId, String notes) {
    var req = new CommandeRequest(clientId, modeleId, null, null, Priorite.NORMALE, null, 1, BigDecimal.ZERO, BigDecimal.ZERO, notes);
    Commande c = build(req, CommandeStatut.BROUILLON, null);
    notificationService.notifyEvent(EvenementNotification.COMMANDE_CREEE, c);
    return toDto(c);
  }

  @Transactional @PreAuthorize("hasAnyRole('SUPER_ADMIN','ADMIN','GERANT','RECEPTIONNISTE')")
  public CommandeDto createFromDevis(Long clientId, Long modeleId, Long compositionId, int quantite, BigDecimal prixUnitaire, BigDecimal remise, Long devisId) {
    var req = new CommandeRequest(clientId, modeleId, compositionId, null, Priorite.NORMALE, null, quantite, prixUnitaire, remise, "Depuis devis");
    Commande c = build(req, CommandeStatut.CONFIRMEE, devisId);
    productionService.ensureFor(c);
    notificationService.notifyEvent(EvenementNotification.COMMANDE_CONFIRMEE, c);
    return toDto(c);
  }

  private Commande build(CommandeRequest req, CommandeStatut statut, Long devisId) {
    Commande c = new Commande();
    c.setNumero(nextNumero());
    c.setClient(clientService.require(req.clientId()));
    if (req.modeleId()!=null) {
      Modele m = modeleService.require(req.modeleId());
      c.setModele(m);
      if (c.getPrixUnitaire()==null || BigDecimal.ZERO.equals(req.prixUnitaire())) c.setPrixUnitaire(m.getPrixIndicatif()==null?BigDecimal.ZERO:m.getPrixIndicatif());
    }
    if (req.compositionId()!=null) c.setComposition(compositionRepository.findById(req.compositionId()).orElse(null));
    if (req.tailleurId()!=null) c.setTailleur(tailleurService.require(req.tailleurId()));
    c.setPriorite(req.priorite()==null?Priorite.NORMALE:req.priorite());
    c.setDatePrevue(req.datePrevue());
    c.setQuantite(Math.max(req.quantite(),1));
    if (req.prixUnitaire()!=null) c.setPrixUnitaire(req.prixUnitaire());
    c.setRemise(req.remise()==null?BigDecimal.ZERO:req.remise());
    c.setNotes(req.notes());
    c.setDevisId(devisId);
    c.setStatut(statut);
    recalc(c);
    Commande saved = commandeRepository.save(c);
    hist(saved, null, statut, "Création");
    auditService.log(null, "CREATE_COMMANDE", "COMMANDE", saved.getId(), saved.getNumero());
    return saved;
  }

  @Transactional @PreAuthorize("hasAnyRole('SUPER_ADMIN','ADMIN','GERANT','RECEPTIONNISTE','TAILLEUR')")
  public CommandeDto changerStatut(Long id, CommandeStatut nouveau, String commentaire) {
    Commande c = require(id);
    CommandeWorkflow.assertTransition(c.getStatut(), nouveau);
    CommandeStatut ancien = c.getStatut();
    c.setStatut(nouveau);
    if (nouveau == CommandeStatut.LIVREE) c.setDateLivraison(LocalDate.now());
    commandeRepository.save(c);
    hist(c, ancien, nouveau, commentaire);
    if (nouveau == CommandeStatut.CONFIRMEE) productionService.ensureFor(c);
    productionService.recordEtape(c, nouveau.name(), commentaire);
    if (nouveau == CommandeStatut.CONFIRMEE) notificationService.notifyEvent(EvenementNotification.COMMANDE_CONFIRMEE, c);
    else if (nouveau == CommandeStatut.PRETE) notificationService.notifyEvent(EvenementNotification.COMMANDE_PRETE, c);
    else if (nouveau == CommandeStatut.LIVREE) notificationService.notifyEvent(EvenementNotification.LIVRAISON, c);
    else notificationService.notifyEvent(EvenementNotification.STATUT_CHANGE, c);
    auditService.log(null, "STATUT_COMMANDE", "COMMANDE", c.getId(), ancien + "->" + nouveau);
    return toDto(c);
  }

  @Transactional @PreAuthorize("hasAnyRole('SUPER_ADMIN','ADMIN','GERANT')")
  public CommandeDto assigner(Long id, Long tailleurId) {
    Commande c = require(id);
    c.setTailleur(tailleurService.require(tailleurId));
    productionService.ensureFor(c);
    return toDto(commandeRepository.save(c));
  }

  private void recalc(Commande c) {
    BigDecimal brut = (c.getPrixUnitaire()==null?BigDecimal.ZERO:c.getPrixUnitaire()).multiply(BigDecimal.valueOf(c.getQuantite()));
    c.setMontantTotal(brut.subtract(c.getRemise()==null?BigDecimal.ZERO:c.getRemise()).max(BigDecimal.ZERO));
  }

  private String nextNumero() {
    String prefix = "CMD-" + Year.now() + "-";
    long n = commandeRepository.countByNumeroStartingWith(prefix) + 1;
    return prefix + String.format("%04d", n);
  }

  private void hist(Commande c, CommandeStatut ancien, CommandeStatut nouveau, String commentaire) {
    CommandeStatutHistorique h = new CommandeStatutHistorique();
    h.setCommande(c); h.setAncien(ancien); h.setNouveau(nouveau); h.setCommentaire(commentaire);
    historiqueRepository.save(h);
  }

  public CommandeDto toDto(Commande c) {
    boolean retard = c.getDatePrevue()!=null && c.getStatut()!=CommandeStatut.LIVREE && c.getStatut()!=CommandeStatut.ANNULEE && c.getDatePrevue().isBefore(LocalDate.now());
    return new CommandeDto(c.getId(), c.getNumero(), c.getClient().getId(), c.getClient().nomComplet(),
      c.getModele()==null?null:c.getModele().getId(), c.getModele()==null?null:c.getModele().getNom(),
      c.getTailleur()==null?null:c.getTailleur().getId(), c.getStatut(), c.getPriorite(), c.getDatePrevue(), c.getDateLivraison(),
      c.getQuantite(), c.getPrixUnitaire(), c.getRemise(), c.getMontantTotal(), c.getNotes(), retard);
  }
}
''')
w("commande/CommandeController.java", '''
package com.couturix.commande;
import com.couturix.common.api.PageResponse;
import com.couturix.commande.dto.*;
import com.couturix.domain.enums.CommandeStatut;
import jakarta.validation.Valid;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
@RestController @RequestMapping("/api/v1/commandes") @RequiredArgsConstructor
@PreAuthorize("hasAnyRole('SUPER_ADMIN','ADMIN','GERANT','RECEPTIONNISTE','TAILLEUR','CAISSIER','COMPTABLE')")
public class CommandeController {
  private final CommandeService service;
  @GetMapping public PageResponse<CommandeDto> list(@RequestParam(required=false) CommandeStatut statut, @RequestParam(required=false) Long clientId, Pageable pageable) { return service.list(statut, clientId, pageable); }
  @GetMapping("/{id}") public CommandeDto get(@PathVariable Long id) { return service.get(id); }
  @PostMapping public CommandeDto create(@Valid @RequestBody CommandeRequest req) { return service.create(req); }
  @PostMapping("/{id}/statut") public CommandeDto statut(@PathVariable Long id, @RequestBody Map<String,String> body) {
    return service.changerStatut(id, CommandeStatut.valueOf(body.get("statut")), body.get("commentaire"));
  }
  @PostMapping("/{id}/assigner") public CommandeDto assigner(@PathVariable Long id, @RequestBody Map<String, Long> body) { return service.assigner(id, body.get("tailleurId")); }
}
''')
print("commande service ok")
