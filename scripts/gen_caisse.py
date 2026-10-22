# -*- coding: utf-8 -*-
from pathlib import Path
ROOT = Path(__file__).resolve().parents[1] / "src" / "main" / "java" / "com" / "couturix"

def w(rel, content):
    p = ROOT / rel
    p.parent.mkdir(parents=True, exist_ok=True)
    p.write_text(content.strip() + "\n", encoding="utf-8")

w("caisse/Caisse.java", '''
package com.couturix.caisse;
import com.couturix.domain.enums.CaisseStatut;
import com.couturix.user.User;
import jakarta.persistence.*;
import java.math.BigDecimal; import java.time.Instant; import java.util.*;
import lombok.Getter; import lombok.Setter;
@Getter @Setter @Entity @Table(name="caisse")
public class Caisse {
  @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
  @Column(name="date_ouverture", nullable=false) private Instant dateOuverture = Instant.now();
  @Column(name="date_cloture") private Instant dateCloture;
  @Column(name="solde_initial", nullable=false, precision=14, scale=2) private BigDecimal soldeInitial = BigDecimal.ZERO;
  @Column(name="solde_theorique", precision=14, scale=2) private BigDecimal soldeTheorique;
  @Column(name="solde_reel", precision=14, scale=2) private BigDecimal soldeReel;
  @Enumerated(EnumType.STRING) @Column(nullable=false, length=20) private CaisseStatut statut = CaisseStatut.OUVERTE;
  @ManyToOne @JoinColumn(name="user_id") private User user;
  private String notes;
  @OneToMany(mappedBy="caisse") private List<CaisseOperation> operations = new ArrayList<>();
}
''')
w("caisse/CaisseOperation.java", '''
package com.couturix.caisse;
import com.couturix.domain.enums.*;
import com.couturix.user.User;
import jakarta.persistence.*;
import java.math.BigDecimal; import java.time.Instant;
import lombok.Getter; import lombok.Setter;
@Getter @Setter @Entity @Table(name="caisse_operations")
public class CaisseOperation {
  @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
  @ManyToOne(optional=false) @JoinColumn(name="caisse_id") private Caisse caisse;
  @Enumerated(EnumType.STRING) @Column(name="type_operation", nullable=false, length=20) private TypeOperationCaisse typeOperation;
  @Enumerated(EnumType.STRING) @Column(nullable=false, length=40) private CategorieOperationCaisse categorie;
  @Column(nullable=false, precision=14, scale=2) private BigDecimal montant;
  @Enumerated(EnumType.STRING) @Column(name="moyen_paiement", nullable=false, length=30) private MoyenPaiement moyenPaiement;
  @Column(nullable=false) private String motif;
  @Column(name="reference_metier", length=80) private String referenceMetier;
  @ManyToOne @JoinColumn(name="user_id") private User user;
  @Column(name="created_at", nullable=false) private Instant createdAt = Instant.now();
}
''')
w("caisse/ClotureCaisse.java", '''
package com.couturix.caisse;
import jakarta.persistence.*;
import java.math.BigDecimal; import java.time.Instant;
import lombok.Getter; import lombok.Setter;
@Getter @Setter @Entity @Table(name="clotures_caisse")
public class ClotureCaisse {
  @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
  @OneToOne(optional=false) @JoinColumn(name="caisse_id") private Caisse caisse;
  @Column(nullable=false, precision=14, scale=2) private BigDecimal ecart = BigDecimal.ZERO;
  @Column(columnDefinition="TEXT") private String commentaires;
  @Column(name="created_at", nullable=false) private Instant createdAt = Instant.now();
}
''')
w("caisse/CaisseRepository.java", "package com.couturix.caisse;\nimport com.couturix.domain.enums.CaisseStatut; import java.util.Optional;\nimport org.springframework.data.jpa.repository.JpaRepository;\npublic interface CaisseRepository extends JpaRepository<Caisse, Long> {\n  Optional<Caisse> findFirstByStatut(CaisseStatut statut);\n}")
w("caisse/CaisseOperationRepository.java", "package com.couturix.caisse;\nimport org.springframework.data.jpa.repository.JpaRepository;\npublic interface CaisseOperationRepository extends JpaRepository<CaisseOperation, Long> {}")
w("caisse/ClotureCaisseRepository.java", "package com.couturix.caisse;\nimport org.springframework.data.jpa.repository.JpaRepository;\npublic interface ClotureCaisseRepository extends JpaRepository<ClotureCaisse, Long> {}")
w("caisse/CaisseService.java", '''
package com.couturix.caisse;
import com.couturix.common.exception.ApiException;
import com.couturix.domain.enums.*;
import java.math.BigDecimal; import java.time.Instant;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service; import org.springframework.transaction.annotation.Transactional;
@Service @RequiredArgsConstructor
public class CaisseService {
  private final CaisseRepository caisseRepository;
  private final CaisseOperationRepository operationRepository;
  private final ClotureCaisseRepository clotureRepository;

  public Caisse courante() { return caisseRepository.findFirstByStatut(CaisseStatut.OUVERTE).orElse(null); }

  @Transactional @PreAuthorize("hasAnyRole('SUPER_ADMIN','ADMIN','GERANT','CAISSIER')")
  public Caisse ouvrir(BigDecimal soldeInitial) {
    if (courante()!=null) throw ApiException.conflict("Une caisse est déjà ouverte");
    Caisse c = new Caisse(); c.setSoldeInitial(soldeInitial==null?BigDecimal.ZERO:soldeInitial); c.setStatut(CaisseStatut.OUVERTE);
    return caisseRepository.save(c);
  }

  @Transactional
  public CaisseOperation operation(TypeOperationCaisse type, CategorieOperationCaisse cat, BigDecimal montant, MoyenPaiement moyen, String motif, String ref) {
    Caisse c = caisseRepository.findFirstByStatut(CaisseStatut.OUVERTE).orElseThrow(() -> ApiException.badRequest("Aucune caisse ouverte"));
    CaisseOperation op = new CaisseOperation();
    op.setCaisse(c); op.setTypeOperation(type); op.setCategorie(cat); op.setMontant(montant); op.setMoyenPaiement(moyen==null?MoyenPaiement.ESPECES:moyen); op.setMotif(motif); op.setReferenceMetier(ref);
    return operationRepository.save(op);
  }

  @Transactional @PreAuthorize("hasAnyRole('SUPER_ADMIN','ADMIN','GERANT','CAISSIER')")
  public Caisse cloturer(Long id, BigDecimal soldeReel, String commentaires) {
    Caisse c = caisseRepository.findById(id).orElseThrow(() -> ApiException.notFound("Caisse introuvable"));
    if (c.getStatut()==CaisseStatut.CLOTUREE) throw ApiException.conflict("Caisse déjà clôturée");
    BigDecimal recettes = c.getOperations().stream().filter(o -> o.getTypeOperation()==TypeOperationCaisse.RECETTE).map(CaisseOperation::getMontant).reduce(BigDecimal.ZERO, BigDecimal::add);
    BigDecimal depenses = c.getOperations().stream().filter(o -> o.getTypeOperation()==TypeOperationCaisse.DEPENSE).map(CaisseOperation::getMontant).reduce(BigDecimal.ZERO, BigDecimal::add);
    BigDecimal theo = c.getSoldeInitial().add(recettes).subtract(depenses);
    c.setSoldeTheorique(theo); c.setSoldeReel(soldeReel); c.setStatut(CaisseStatut.CLOTUREE); c.setDateCloture(Instant.now());
    ClotureCaisse cl = new ClotureCaisse(); cl.setCaisse(c); cl.setEcart(soldeReel.subtract(theo)); cl.setCommentaires(commentaires);
    clotureRepository.save(cl);
    return caisseRepository.save(c);
  }
}
''')
w("caisse/CaisseController.java", '''
package com.couturix.caisse;
import com.couturix.domain.enums.*;
import java.math.BigDecimal; import java.util.List; import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
@RestController @RequestMapping("/api/v1/caisse") @RequiredArgsConstructor
@PreAuthorize("hasAnyRole('SUPER_ADMIN','ADMIN','GERANT','CAISSIER','COMPTABLE')")
public class CaisseController {
  private final CaisseService service; private final CaisseRepository repo;
  @GetMapping public List<Caisse> list() { return repo.findAll(); }
  @GetMapping("/courante") public Caisse courante() { return service.courante(); }
  @PostMapping("/ouvrir") public Caisse ouvrir(@RequestBody Map<String, BigDecimal> body) { return service.ouvrir(body.get("soldeInitial")); }
  @PostMapping("/{id}/operations") public CaisseOperation op(@PathVariable Long id, @RequestBody CaisseOperation body) {
    return service.operation(body.getTypeOperation(), body.getCategorie(), body.getMontant(), body.getMoyenPaiement(), body.getMotif(), body.getReferenceMetier());
  }
  @PostMapping("/{id}/cloturer") public Caisse cloturer(@PathVariable Long id, @RequestBody Map<String, String> body) {
    return service.cloturer(id, new BigDecimal(body.get("soldeReel")), body.get("commentaires"));
  }
}
''')

w("paiement/Paiement.java", '''
package com.couturix.paiement;
import com.couturix.client.Client;
import com.couturix.commande.Commande;
import com.couturix.domain.enums.MoyenPaiement;
import com.couturix.user.User;
import jakarta.persistence.*;
import java.math.BigDecimal; import java.time.Instant; import java.time.LocalDate;
import lombok.Getter; import lombok.Setter;
@Getter @Setter @Entity @Table(name="paiements")
public class Paiement {
  @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
  @ManyToOne @JoinColumn(name="commande_id") private Commande commande;
  @ManyToOne(optional=false) @JoinColumn(name="client_id") private Client client;
  @Column(nullable=false, precision=14, scale=2) private BigDecimal montant;
  @Enumerated(EnumType.STRING) @Column(nullable=false, length=30) private MoyenPaiement moyen;
  @Column(name="date_paiement", nullable=false) private LocalDate datePaiement = LocalDate.now();
  private String reference; private String notes;
  @ManyToOne @JoinColumn(name="user_id") private User user;
  @Column(name="created_at", nullable=false) private Instant createdAt = Instant.now();
}
''')
w("paiement/PaiementRepository.java", "package com.couturix.paiement;\nimport java.time.Instant; import java.util.List;\nimport org.springframework.data.jpa.repository.JpaRepository;\npublic interface PaiementRepository extends JpaRepository<Paiement, Long> {\n  List<Paiement> findByClientIdOrderByCreatedAtDesc(Long clientId);\n  List<Paiement> findByCreatedAtBetween(Instant from, Instant to);\n}")
w("paiement/PaiementService.java", '''
package com.couturix.paiement;
import com.couturix.caisse.CaisseService;
import com.couturix.client.Client; import com.couturix.client.ClientService;
import com.couturix.commande.CommandeService;
import com.couturix.domain.enums.*;
import com.couturix.notification.NotificationService;
import java.math.BigDecimal; import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service; import org.springframework.transaction.annotation.Transactional;
@Service @RequiredArgsConstructor
public class PaiementService {
  private final PaiementRepository repo;
  private final ClientService clientService;
  private final CommandeService commandeService;
  private final CaisseService caisseService;
  private final NotificationService notificationService;

  public List<Paiement> list() { return repo.findAll(); }

  @Transactional @PreAuthorize("hasAnyRole('SUPER_ADMIN','ADMIN','GERANT','CAISSIER')")
  public Paiement enregistrer(Long clientId, Long commandeId, BigDecimal montant, MoyenPaiement moyen, String reference) {
    Client client = clientService.require(clientId);
    Paiement p = new Paiement(); p.setClient(client); p.setMontant(montant); p.setMoyen(moyen==null?MoyenPaiement.ESPECES:moyen); p.setReference(reference);
    if (commandeId!=null) p.setCommande(commandeService.require(commandeId));
    client.setSolde(client.getSolde().subtract(montant));
    Paiement saved = repo.save(p);
    if (caisseService.courante()!=null) {
      caisseService.operation(TypeOperationCaisse.RECETTE, CategorieOperationCaisse.PAIEMENT_CLIENT, montant, moyen, "Paiement client", saved.getId().toString());
    }
    if (p.getCommande()!=null) notificationService.notifyEvent(EvenementNotification.PAIEMENT, p.getCommande());
    return saved;
  }
}
''')
w("paiement/PaiementController.java", '''
package com.couturix.paiement;
import com.couturix.domain.enums.MoyenPaiement;
import java.math.BigDecimal; import java.util.List; import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
@RestController @RequestMapping("/api/v1/paiements") @RequiredArgsConstructor
@PreAuthorize("hasAnyRole('SUPER_ADMIN','ADMIN','GERANT','CAISSIER','COMPTABLE')")
public class PaiementController {
  private final PaiementService service;
  @GetMapping public List<Paiement> list() { return service.list(); }
  @PostMapping public Paiement create(@RequestBody Map<String, Object> body) {
    Long commandeId = body.get("commandeId")==null?null:Long.valueOf(body.get("commandeId").toString());
    MoyenPaiement moyen = body.get("moyen")==null?MoyenPaiement.ESPECES:MoyenPaiement.valueOf(body.get("moyen").toString());
    return service.enregistrer(Long.valueOf(body.get("clientId").toString()), commandeId, new BigDecimal(body.get("montant").toString()), moyen, (String) body.get("reference"));
  }
}
''')
print("caisse+paiement ok")
