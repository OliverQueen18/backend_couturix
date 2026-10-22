package com.couturix.stock;
import com.couturix.admin.ParametreRepository;
import com.couturix.common.exception.ApiException;
import com.couturix.config.CouturixProperties;
import com.couturix.domain.enums.TypeMouvementStock;
import com.couturix.security.SecurityUtils;
import com.couturix.stock.dto.InventaireRequest;
import com.couturix.stock.dto.InventaireResultatDto;
import com.couturix.stock.dto.MouvementDto;
import com.couturix.stock.dto.MouvementRequest;
import com.couturix.user.UserRepository;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service @RequiredArgsConstructor
public class StockService {
  private static final ZoneId ZONE = ZoneId.of("Africa/Brazzaville");
  private final ArticleStockRepository articleRepository;
  private final MouvementStockRepository mouvementRepository;
  private final CategorieStockRepository categorieRepository;
  private final ParametreRepository parametreRepository;
  private final CouturixProperties properties;
  private final UserRepository userRepository;

  public List<ArticleStock> articles() { return articleRepository.findAll(); }
  public List<CategorieStock> categories() { return categorieRepository.findAll(); }
  public List<ArticleStock> alertes() { return articleRepository.findAlertes(); }
  public BigDecimal valorisation() {
    return articleRepository.findAll().stream()
        .map(a -> nvl(a.getStockActuel()).multiply(nvl(a.getPrixAchat())))
        .reduce(BigDecimal.ZERO, BigDecimal::add);
  }

  public List<MouvementDto> mouvements(Long articleId) {
    List<MouvementStock> rows = articleId == null
        ? mouvementRepository.findAllByOrderByCreatedAtDesc()
        : mouvementRepository.findByArticleIdOrderByCreatedAtDesc(articleId);
    return rows.stream().limit(300).map(this::toDto).toList();
  }

  @Transactional @PreAuthorize("hasAnyRole('SUPER_ADMIN','ADMIN','GERANT','MAGASINIER')")
  public ArticleStock saveArticle(ArticleStock incoming) {
    if (incoming.getId() != null) {
      ArticleStock existing = articleRepository.findById(incoming.getId())
          .orElseThrow(() -> ApiException.notFound("Article introuvable"));
      existing.setNom(incoming.getNom());
      existing.setSku(incoming.getSku());
      if (incoming.getUnite() != null) existing.setUnite(incoming.getUnite());
      if (incoming.getStockMinimum() != null) existing.setStockMinimum(incoming.getStockMinimum());
      if (incoming.getPrixAchat() != null) existing.setPrixAchat(incoming.getPrixAchat());
      existing.setPrixVente(incoming.getPrixVente());
      if (incoming.getCategorie() != null) existing.setCategorie(incoming.getCategorie());
      if (incoming.getFournisseur() != null) existing.setFournisseur(incoming.getFournisseur());
      existing.setUpdatedAt(Instant.now());
      return articleRepository.save(existing);
    }
    if (incoming.getStockActuel() == null) incoming.setStockActuel(BigDecimal.ZERO);
    return articleRepository.save(incoming);
  }

  @Transactional @PreAuthorize("hasAnyRole('SUPER_ADMIN','ADMIN','GERANT','MAGASINIER')")
  public CategorieStock saveCategorie(CategorieStock c) { return categorieRepository.save(c); }

  @Transactional @PreAuthorize("hasAnyRole('SUPER_ADMIN','ADMIN','GERANT','MAGASINIER')")
  public MouvementStock mouvement(MouvementRequest req) {
    return mouvement(req, null);
  }

  @Transactional @PreAuthorize("hasAnyRole('SUPER_ADMIN','ADMIN','GERANT','MAGASINIER')")
  public InventaireResultatDto inventaire(List<InventaireRequest> lignes) {
    String reference = "INV-" + DateTimeFormatter.ofPattern("yyyyMMdd-HHmm").withZone(ZONE).format(Instant.now());
    List<MouvementDto> mouvements = new ArrayList<>();
    for (InventaireRequest ligne : lignes) {
      ArticleStock article = articleRepository.findByIdForUpdate(ligne.articleId())
          .orElseThrow(() -> ApiException.notFound("Article introuvable"));
      if (nvl(article.getStockActuel()).compareTo(nvl(ligne.quantiteReelle())) == 0) {
        continue;
      }
      MouvementStock mv = mouvement(new MouvementRequest(
          ligne.articleId(), TypeMouvementStock.AJUSTEMENT, ligne.quantiteReelle(), "Inventaire physique", reference), reference);
      mouvements.add(toDto(mv));
    }
    return new InventaireResultatDto(reference, lignes.size(), mouvements.size(), mouvements);
  }

  public boolean allowNegative() {
    if (properties.stock().allowNegative()) return true;
    return parametreRepository.findById("stock.allowNegative").map(p -> "true".equalsIgnoreCase(p.getValeur())).orElse(false);
  }

  private MouvementStock mouvement(MouvementRequest req, String reference) {
    ArticleStock article = articleRepository.findByIdForUpdate(req.articleId()).orElseThrow(() -> ApiException.notFound("Article introuvable"));
    BigDecimal avant = nvl(article.getStockActuel());
    BigDecimal delta = req.quantite();
    TypeMouvementStock type = req.type();
    BigDecimal apres;
    if (type == TypeMouvementStock.ENTREE || type == TypeMouvementStock.RETOUR) {
      apres = avant.add(delta);
    } else if (type == TypeMouvementStock.AJUSTEMENT) {
      apres = delta;
      delta = apres.subtract(avant);
    } else {
      apres = avant.subtract(delta.abs());
    }
    if (apres.compareTo(BigDecimal.ZERO) < 0 && !allowNegative()) {
      throw ApiException.conflict("Stock insuffisant pour " + article.getNom());
    }
    article.setStockActuel(apres);
    article.setUpdatedAt(Instant.now());
    articleRepository.save(article);
    MouvementStock mv = new MouvementStock();
    mv.setArticle(article);
    mv.setTypeMouvement(type);
    mv.setQuantite(delta.abs());
    mv.setStockAvant(avant);
    mv.setStockApres(apres);
    mv.setMotif(req.motif());
    mv.setReferenceMetier(reference != null ? reference : req.referenceMetier());
    SecurityUtils.currentUserOptional().ifPresent(p ->
        userRepository.findById(p.getId()).ifPresent(mv::setUser));
    return mouvementRepository.save(mv);
  }

  private MouvementDto toDto(MouvementStock mv) {
    ArticleStock article = mv.getArticle();
    return new MouvementDto(
        mv.getId(),
        article == null ? null : article.getId(),
        article == null ? null : article.getNom(),
        mv.getTypeMouvement(),
        mv.getQuantite(),
        mv.getStockAvant(),
        mv.getStockApres(),
        mv.getMotif(),
        mv.getReferenceMetier(),
        mv.getCreatedAt()
    );
  }

  private static BigDecimal nvl(BigDecimal v) {
    return v == null ? BigDecimal.ZERO : v;
  }
}
