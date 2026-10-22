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
    long pieces = productionRepository.findValidatedInPeriod(t.getId(),
            debut.atStartOfDay().toInstant(java.time.ZoneOffset.UTC),
            fin.plusDays(1).atStartOfDay().toInstant(java.time.ZoneOffset.UTC)).size();
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
