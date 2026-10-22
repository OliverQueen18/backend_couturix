# -*- coding: utf-8 -*-
from pathlib import Path
ROOT = Path(__file__).resolve().parents[1] / "src" / "main" / "java" / "com" / "couturix"

def w(rel, content):
    p = ROOT / rel
    p.parent.mkdir(parents=True, exist_ok=True)
    p.write_text(content.strip() + "\n", encoding="utf-8")

w("paie/Avance.java", '''
package com.couturix.paie;
import com.couturix.tailleur.Tailleur;
import jakarta.persistence.*;
import java.math.BigDecimal; import java.time.Instant; import java.time.LocalDate;
import lombok.Getter; import lombok.Setter;
@Getter @Setter @Entity @Table(name="avances")
public class Avance {
  @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
  @ManyToOne(optional=false) @JoinColumn(name="tailleur_id") private Tailleur tailleur;
  @Column(nullable=false, precision=14, scale=2) private BigDecimal montant;
  @Column(name="date_avance", nullable=false) private LocalDate dateAvance = LocalDate.now();
  private String motif;
  @Column(nullable=false, length=20) private String statut = "EN_COURS";
  @Column(name="created_at", nullable=false) private Instant createdAt = Instant.now();
}
''')
w("paie/Paie.java", '''
package com.couturix.paie;
import com.couturix.domain.enums.PaieStatut;
import com.couturix.tailleur.Tailleur;
import jakarta.persistence.*;
import java.math.BigDecimal; import java.time.Instant; import java.time.LocalDate;
import lombok.Getter; import lombok.Setter;
@Getter @Setter @Entity @Table(name="paies")
public class Paie {
  @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
  @ManyToOne(optional=false) @JoinColumn(name="tailleur_id") private Tailleur tailleur;
  @Column(name="periode_debut", nullable=false) private LocalDate periodeDebut;
  @Column(name="periode_fin", nullable=false) private LocalDate periodeFin;
  @Column(name="montant_brut", nullable=false, precision=14, scale=2) private BigDecimal montantBrut = BigDecimal.ZERO;
  @Column(nullable=false, precision=14, scale=2) private BigDecimal primes = BigDecimal.ZERO;
  @Column(nullable=false, precision=14, scale=2) private BigDecimal retenues = BigDecimal.ZERO;
  @Column(nullable=false, precision=14, scale=2) private BigDecimal avances = BigDecimal.ZERO;
  @Column(name="montant_net", nullable=false, precision=14, scale=2) private BigDecimal montantNet = BigDecimal.ZERO;
  @Enumerated(EnumType.STRING) @Column(nullable=false, length=20) private PaieStatut statut = PaieStatut.BROUILLON;
  @Column(columnDefinition="TEXT") private String notes;
  @Column(name="created_at", nullable=false) private Instant createdAt = Instant.now();
  @Column(name="updated_at", nullable=false) private Instant updatedAt = Instant.now();
}
''')
w("paie/AvanceRepository.java", "package com.couturix.paie;\nimport java.util.List;\nimport org.springframework.data.jpa.repository.JpaRepository;\npublic interface AvanceRepository extends JpaRepository<Avance, Long> {\n  List<Avance> findByTailleurIdAndStatut(Long tailleurId, String statut);\n}")
w("paie/PaieRepository.java", "package com.couturix.paie;\nimport org.springframework.data.jpa.repository.JpaRepository;\npublic interface PaieRepository extends JpaRepository<Paie, Long> {}")
w("paie/PaieService.java", '''
package com.couturix.paie;
import com.couturix.caisse.CaisseService;
import com.couturix.commande.CommandeRepository;
import com.couturix.common.exception.ApiException;
import com.couturix.domain.enums.*;
import com.couturix.production.ProductionRepository;
import com.couturix.tailleur.Tailleur;
import com.couturix.tailleur.TailleurService;
import java.math.BigDecimal; import java.time.LocalDate; import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service; import org.springframework.transaction.annotation.Transactional;
@Service @RequiredArgsConstructor
public class PaieService {
  private final PaieRepository paieRepository;
  private final AvanceRepository avanceRepository;
  private final TailleurService tailleurService;
  private final ProductionRepository productionRepository;
  private final CommandeRepository commandeRepository;
  private final CaisseService caisseService;

  public List<Paie> list() { return paieRepository.findAll(); }
  public List<Avance> avances() { return avanceRepository.findAll(); }

  @Transactional @PreAuthorize("hasAnyRole('SUPER_ADMIN','ADMIN','GERANT','COMPTABLE')")
  public Avance createAvance(Long tailleurId, BigDecimal montant, String motif) {
    Avance a = new Avance(); a.setTailleur(tailleurService.require(tailleurId)); a.setMontant(montant); a.setMotif(motif);
    if (caisseService.courante()!=null) caisseService.operation(TypeOperationCaisse.DEPENSE, CategorieOperationCaisse.AVANCE, montant, MoyenPaiement.ESPECES, "Avance tailleur", null);
    return avanceRepository.save(a);
  }

  @Transactional @PreAuthorize("hasAnyRole('SUPER_ADMIN','ADMIN','GERANT','COMPTABLE')")
  public Paie calculer(Long tailleurId, LocalDate debut, LocalDate fin, BigDecimal primes, BigDecimal retenues) {
    Tailleur t = tailleurService.require(tailleurId);
    BigDecimal brut = BigDecimal.ZERO;
    long pieces = productionRepository.findByTailleurId(t.getId()).stream()
        .filter(p -> p.getCommande()!=null && p.getCommande().getStatut()==CommandeStatut.LIVREE || p.getCommande()!=null && p.getCommande().getStatut()==CommandeStatut.PRETE)
        .count();
    BigDecimal ca = commandeRepository.findAll().stream()
        .filter(c -> c.getTailleur()!=null && c.getTailleur().getId().equals(t.getId()))
        .filter(c -> c.getCreatedAt()!=null && !c.getCreatedAt().isBefore(debut.atStartOfDay().toInstant(java.time.ZoneOffset.UTC)))
        .map(c -> c.getMontantTotal()==null?BigDecimal.ZERO:c.getMontantTotal()).reduce(BigDecimal.ZERO, BigDecimal::add);
    switch (t.getModeRemuneration()) {
      case FIXE -> brut = nvl(t.getSalaireFixe());
      case PIECE -> brut = nvl(t.getTauxPiece()).multiply(BigDecimal.valueOf(pieces));
      case POURCENTAGE -> brut = ca.multiply(nvl(t.getPourcentage())).divide(BigDecimal.valueOf(100), 2, java.math.RoundingMode.HALF_UP);
      case MIXTE -> brut = nvl(t.getSalaireFixe()).add(nvl(t.getTauxPiece()).multiply(BigDecimal.valueOf(pieces)));
    }
    BigDecimal av = avanceRepository.findByTailleurIdAndStatut(t.getId(), "EN_COURS").stream().map(Avance::getMontant).reduce(BigDecimal.ZERO, BigDecimal::add);
    Paie p = new Paie(); p.setTailleur(t); p.setPeriodeDebut(debut); p.setPeriodeFin(fin);
    p.setMontantBrut(brut); p.setPrimes(nvl(primes)); p.setRetenues(nvl(retenues)); p.setAvances(av);
    p.setMontantNet(brut.add(nvl(primes)).subtract(nvl(retenues)).subtract(av).max(BigDecimal.ZERO));
    p.setStatut(PaieStatut.BROUILLON);
    return paieRepository.save(p);
  }

  @Transactional @PreAuthorize("hasAnyRole('SUPER_ADMIN','ADMIN','GERANT','COMPTABLE')")
  public Paie valider(Long id) {
    Paie p = require(id);
    if (p.getStatut()!=PaieStatut.BROUILLON) throw ApiException.conflict("Seule une paie brouillon peut être validée");
    p.setStatut(PaieStatut.VALIDEE); return paieRepository.save(p);
  }

  @Transactional @PreAuthorize("hasAnyRole('SUPER_ADMIN','ADMIN','GERANT','COMPTABLE')")
  public Paie payer(Long id) {
    Paie p = require(id);
    if (p.getStatut()!=PaieStatut.VALIDEE) throw ApiException.conflict("La paie doit être validée avant paiement");
    p.setStatut(PaieStatut.PAYEE);
    avanceRepository.findByTailleurIdAndStatut(p.getTailleur().getId(), "EN_COURS").forEach(a -> { a.setStatut("IMPUTEE"); avanceRepository.save(a); });
    if (caisseService.courante()==null) throw ApiException.badRequest("Ouvrez la caisse avant de payer une paie");
    caisseService.operation(TypeOperationCaisse.DEPENSE, CategorieOperationCaisse.SALAIRE, p.getMontantNet(), MoyenPaiement.ESPECES, "Paie "+p.getTailleur().nomComplet(), p.getId().toString());
    return paieRepository.save(p);
  }

  private Paie require(Long id) { return paieRepository.findById(id).orElseThrow(() -> ApiException.notFound("Paie introuvable")); }
  private BigDecimal nvl(BigDecimal v) { return v==null?BigDecimal.ZERO:v; }
}
''')
w("paie/PaieController.java", '''
package com.couturix.paie;
import java.math.BigDecimal; import java.time.LocalDate; import java.util.List; import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
@RestController @RequiredArgsConstructor
@PreAuthorize("hasAnyRole('SUPER_ADMIN','ADMIN','GERANT','COMPTABLE')")
public class PaieController {
  private final PaieService service;
  @GetMapping("/api/v1/paies") public List<Paie> paies() { return service.list(); }
  @GetMapping("/api/v1/avances") public List<Avance> avances() { return service.avances(); }
  @PostMapping("/api/v1/avances") public Avance avance(@RequestBody Map<String,String> body) {
    return service.createAvance(Long.valueOf(body.get("tailleurId")), new BigDecimal(body.get("montant")), body.get("motif"));
  }
  @PostMapping("/api/v1/paies/calculer") public Paie calculer(@RequestBody Map<String,String> body) {
    return service.calculer(Long.valueOf(body.get("tailleurId")), LocalDate.parse(body.get("debut")), LocalDate.parse(body.get("fin")),
      body.get("primes")==null?BigDecimal.ZERO:new BigDecimal(body.get("primes")),
      body.get("retenues")==null?BigDecimal.ZERO:new BigDecimal(body.get("retenues")));
  }
  @PostMapping("/api/v1/paies/{id}/valider") public Paie valider(@PathVariable Long id) { return service.valider(id); }
  @PostMapping("/api/v1/paies/{id}/payer") public Paie payer(@PathVariable Long id) { return service.payer(id); }
}
''')
print("paie ok")
