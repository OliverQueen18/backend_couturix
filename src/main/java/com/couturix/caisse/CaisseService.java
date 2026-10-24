package com.couturix.caisse;
import com.couturix.common.exception.ApiException;
import com.couturix.domain.enums.*;
import java.math.BigDecimal; import java.time.Instant;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service; import org.springframework.transaction.annotation.Transactional;
@Service @RequiredArgsConstructor
public class CaisseService {
  private final CaisseRepository caisseRepository;
  private final CaisseOperationRepository operationRepository;
  private final ClotureCaisseRepository clotureRepository;

  public Caisse courante() { return caisseRepository.findFirstByStatut(CaisseStatut.OUVERTE).orElse(null); }

  @Transactional(readOnly = true)
  public BigDecimal soldeCourant() {
    Caisse c = courante();
    if (c == null) return BigDecimal.ZERO;
    BigDecimal recettes = BigDecimal.ZERO;
    BigDecimal depenses = BigDecimal.ZERO;
    for (CaisseOperation o : operationRepository.findByCaisseId(c.getId())) {
      if (o.getTypeOperation() == TypeOperationCaisse.RECETTE) recettes = recettes.add(nvl(o.getMontant()));
      else depenses = depenses.add(nvl(o.getMontant()));
    }
    return nvl(c.getSoldeInitial()).add(recettes).subtract(depenses);
  }

  @Transactional(readOnly = true)
  public java.util.Map<String, Object> rapport(Instant from, Instant to) {
    Instant f = from == null ? Instant.now().minus(java.time.Duration.ofDays(30)) : from;
    Instant t = to == null ? Instant.now() : to;
    BigDecimal recettes = nvl(operationRepository.sumByTypeBetween(TypeOperationCaisse.RECETTE, f, t));
    BigDecimal depenses = nvl(operationRepository.sumByTypeBetween(TypeOperationCaisse.DEPENSE, f, t));
    return java.util.Map.of("from", f, "to", t, "recettes", recettes, "depenses", depenses, "soldeNet", recettes.subtract(depenses));
  }

  private static BigDecimal nvl(BigDecimal v) { return v == null ? BigDecimal.ZERO : v; }

  @Transactional @PreAuthorize("hasAnyRole('SUPER_ADMIN','ADMIN','GERANT','CAISSIER')")
  public Caisse ouvrir(BigDecimal soldeInitial) {
    if (courante()!=null) throw ApiException.conflict("Une caisse est déjà ouverte");
    Caisse c = new Caisse(); c.setSoldeInitial(soldeInitial==null?BigDecimal.ZERO:soldeInitial); c.setStatut(CaisseStatut.OUVERTE);
    return caisseRepository.save(c);
  }

  public List<String> categoriesOperation() {
    LinkedHashSet<String> all = new LinkedHashSet<>(List.of(
        CategorieOperationCaisse.PAIEMENT_CLIENT.name(),
        CategorieOperationCaisse.ACHAT.name(),
        CategorieOperationCaisse.SALAIRE.name(),
        CategorieOperationCaisse.AVANCE.name(),
        CategorieOperationCaisse.AUTRE.name()));
    for (String value : operationRepository.findDistinctCategories()) {
      if (value != null && !value.isBlank()) {
        all.add(value.trim());
      }
    }
    return List.copyOf(all);
  }

  @Transactional
  public CaisseOperation operation(TypeOperationCaisse type, CategorieOperationCaisse cat, BigDecimal montant, MoyenPaiement moyen, String motif, String ref) {
    return operation(type, cat == null ? null : cat.name(), montant, moyen, motif, ref);
  }

  @Transactional
  public CaisseOperation operation(TypeOperationCaisse type, String cat, BigDecimal montant, MoyenPaiement moyen, String motif, String ref) {
    Caisse c = caisseRepository.findFirstByStatut(CaisseStatut.OUVERTE).orElseThrow(() -> ApiException.badRequest("Aucune caisse ouverte"));
    CaisseOperation op = new CaisseOperation();
    op.setCaisse(c); op.setTypeOperation(type); op.setCategorie(normalizeCategorie(cat)); op.setMontant(montant); op.setMoyenPaiement(moyen==null?MoyenPaiement.ESPECES:moyen); op.setMotif(motif); op.setReferenceMetier(ref);
    return operationRepository.save(op);
  }

  String normalizeCategorie(String raw) {
    if (raw == null || raw.isBlank()) {
      return CategorieOperationCaisse.AUTRE.name();
    }
    String value = raw.trim().replaceAll("\\s+", " ");
    if (value.length() > 80) {
      throw ApiException.badRequest("Catégorie trop longue (80 caractères maximum)");
    }
    String key = value.toLowerCase(Locale.FRENCH);
    for (CategorieOperationCaisse known : CategorieOperationCaisse.values()) {
      if (known.name().equalsIgnoreCase(value) || label(known).equalsIgnoreCase(key)) {
        return known.name();
      }
    }
    return value;
  }

  private static String label(CategorieOperationCaisse categorie) {
    return switch (categorie) {
      case PAIEMENT_CLIENT -> "Paiement client";
      case ACHAT -> "Achat";
      case SALAIRE -> "Salaire";
      case AVANCE -> "Avance";
      case AUTRE -> "Autre";
    };
  }

  @Transactional @PreAuthorize("hasAnyRole('SUPER_ADMIN','ADMIN','GERANT','CAISSIER')")
  public Caisse cloturer(Long id, BigDecimal soldeReel, String commentaires) {
    Caisse c = caisseRepository.findById(id).orElseThrow(() -> ApiException.notFound("Caisse introuvable"));
    if (c.getStatut()==CaisseStatut.CLOTUREE) throw ApiException.conflict("Caisse déjà clôturée");
    var ops = operationRepository.findByCaisseId(c.getId());
    BigDecimal recettes = ops.stream().filter(o -> o.getTypeOperation()==TypeOperationCaisse.RECETTE).map(CaisseOperation::getMontant).reduce(BigDecimal.ZERO, BigDecimal::add);
    BigDecimal depenses = ops.stream().filter(o -> o.getTypeOperation()==TypeOperationCaisse.DEPENSE).map(CaisseOperation::getMontant).reduce(BigDecimal.ZERO, BigDecimal::add);
    BigDecimal theo = c.getSoldeInitial().add(recettes).subtract(depenses);
    c.setSoldeTheorique(theo); c.setSoldeReel(soldeReel); c.setStatut(CaisseStatut.CLOTUREE); c.setDateCloture(Instant.now());
    ClotureCaisse cl = new ClotureCaisse(); cl.setCaisse(c); cl.setEcart(soldeReel.subtract(theo)); cl.setCommentaires(commentaires);
    clotureRepository.save(cl);
    return caisseRepository.save(c);
  }
}
