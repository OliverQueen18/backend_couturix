# -*- coding: utf-8 -*-
from pathlib import Path
ROOT = Path(__file__).resolve().parents[1] / "src" / "main" / "java" / "com" / "couturix"

def w(rel, content):
    p = ROOT / rel
    p.parent.mkdir(parents=True, exist_ok=True)
    p.write_text(content.strip() + "\n", encoding="utf-8")

w("stock/CategorieStock.java", '''
package com.couturix.stock;
import jakarta.persistence.*;
import lombok.Getter; import lombok.Setter;
@Getter @Setter @Entity @Table(name="categories_stock")
public class CategorieStock {
  @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
  @Column(nullable=false, unique=true, length=120) private String nom;
}
''')
w("stock/ArticleStock.java", '''
package com.couturix.stock;
import com.couturix.domain.enums.UniteStock;
import com.couturix.fournisseur.Fournisseur;
import jakarta.persistence.*;
import java.math.BigDecimal; import java.time.Instant;
import lombok.Getter; import lombok.Setter;
@Getter @Setter @Entity @Table(name="articles_stock")
public class ArticleStock {
  @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
  @ManyToOne @JoinColumn(name="categorie_id") private CategorieStock categorie;
  @ManyToOne @JoinColumn(name="fournisseur_id") private Fournisseur fournisseur;
  @Column(nullable=false, length=180) private String nom;
  @Column(nullable=false, unique=true, length=60) private String sku;
  @Enumerated(EnumType.STRING) @Column(nullable=false, length=30) private UniteStock unite = UniteStock.UNITE;
  @Column(name="stock_actuel", nullable=false, precision=14, scale=3) private BigDecimal stockActuel = BigDecimal.ZERO;
  @Column(name="stock_minimum", nullable=false, precision=14, scale=3) private BigDecimal stockMinimum = BigDecimal.ZERO;
  @Column(name="prix_achat", nullable=false, precision=14, scale=2) private BigDecimal prixAchat = BigDecimal.ZERO;
  @Column(name="prix_vente", precision=14, scale=2) private BigDecimal prixVente;
  @Column(nullable=false) private boolean actif = true;
  @Column(name="created_at", nullable=false) private Instant createdAt = Instant.now();
  @Column(name="updated_at", nullable=false) private Instant updatedAt = Instant.now();
}
''')
w("stock/MouvementStock.java", '''
package com.couturix.stock;
import com.couturix.domain.enums.TypeMouvementStock;
import com.couturix.user.User;
import jakarta.persistence.*;
import java.math.BigDecimal; import java.time.Instant;
import lombok.Getter; import lombok.Setter;
@Getter @Setter @Entity @Table(name="mouvements_stock")
public class MouvementStock {
  @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
  @ManyToOne(optional=false) @JoinColumn(name="article_id") private ArticleStock article;
  @Enumerated(EnumType.STRING) @Column(name="type_mouvement", nullable=false, length=40) private TypeMouvementStock typeMouvement;
  @Column(nullable=false, precision=14, scale=3) private BigDecimal quantite;
  @Column(name="stock_avant", nullable=false, precision=14, scale=3) private BigDecimal stockAvant;
  @Column(name="stock_apres", nullable=false, precision=14, scale=3) private BigDecimal stockApres;
  private String motif;
  @Column(name="reference_metier", length=80) private String referenceMetier;
  @ManyToOne @JoinColumn(name="user_id") private User user;
  @Column(name="created_at", nullable=false) private Instant createdAt = Instant.now();
}
''')
w("stock/CategorieStockRepository.java", "package com.couturix.stock;\nimport org.springframework.data.jpa.repository.JpaRepository;\npublic interface CategorieStockRepository extends JpaRepository<CategorieStock, Long> {}")
w("stock/ArticleStockRepository.java", '''
package com.couturix.stock;
import jakarta.persistence.LockModeType;
import java.util.List; import java.util.Optional;
import org.springframework.data.jpa.repository.*;
public interface ArticleStockRepository extends JpaRepository<ArticleStock, Long> {
  @Lock(LockModeType.PESSIMISTIC_WRITE) @Query("select a from ArticleStock a where a.id = :id")
  Optional<ArticleStock> findByIdForUpdate(Long id);
  @Query("select a from ArticleStock a where a.stockActuel <= a.stockMinimum and a.actif = true")
  List<ArticleStock> findAlertes();
}
''')
w("stock/MouvementStockRepository.java", "package com.couturix.stock;\nimport java.util.List;\nimport org.springframework.data.jpa.repository.JpaRepository;\npublic interface MouvementStockRepository extends JpaRepository<MouvementStock, Long> {\n  List<MouvementStock> findByArticleIdOrderByCreatedAtDesc(Long articleId);\n}")
w("stock/dto/MouvementRequest.java", '''
package com.couturix.stock.dto;
import com.couturix.domain.enums.TypeMouvementStock;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
public record MouvementRequest(@NotNull Long articleId, @NotNull TypeMouvementStock type, @NotNull BigDecimal quantite, String motif, String referenceMetier) {}
''')
w("stock/StockService.java", '''
package com.couturix.stock;
import com.couturix.admin.ParametreRepository;
import com.couturix.common.exception.ApiException;
import com.couturix.config.CouturixProperties;
import com.couturix.domain.enums.TypeMouvementStock;
import com.couturix.stock.dto.MouvementRequest;
import java.math.BigDecimal; import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service; import org.springframework.transaction.annotation.Transactional;

@Service @RequiredArgsConstructor
public class StockService {
  private final ArticleStockRepository articleRepository;
  private final MouvementStockRepository mouvementRepository;
  private final CategorieStockRepository categorieRepository;
  private final ParametreRepository parametreRepository;
  private final CouturixProperties properties;

  public List<ArticleStock> articles() { return articleRepository.findAll(); }
  public List<CategorieStock> categories() { return categorieRepository.findAll(); }
  public List<ArticleStock> alertes() { return articleRepository.findAlertes(); }
  public BigDecimal valorisation() {
    return articleRepository.findAll().stream().map(a -> a.getStockActuel().multiply(a.getPrixAchat())).reduce(BigDecimal.ZERO, BigDecimal::add);
  }

  @Transactional @PreAuthorize("hasAnyRole('SUPER_ADMIN','ADMIN','GERANT','MAGASINIER')")
  public ArticleStock saveArticle(ArticleStock a) { return articleRepository.save(a); }

  @Transactional @PreAuthorize("hasAnyRole('SUPER_ADMIN','ADMIN','GERANT','MAGASINIER')")
  public CategorieStock saveCategorie(CategorieStock c) { return categorieRepository.save(c); }

  @Transactional @PreAuthorize("hasAnyRole('SUPER_ADMIN','ADMIN','GERANT','MAGASINIER')")
  public MouvementStock mouvement(MouvementRequest req) {
    ArticleStock article = articleRepository.findByIdForUpdate(req.articleId()).orElseThrow(() -> ApiException.notFound("Article introuvable"));
    BigDecimal avant = article.getStockActuel();
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
    articleRepository.save(article);
    MouvementStock mv = new MouvementStock();
    mv.setArticle(article); mv.setTypeMouvement(type); mv.setQuantite(delta.abs());
    mv.setStockAvant(avant); mv.setStockApres(apres); mv.setMotif(req.motif()); mv.setReferenceMetier(req.referenceMetier());
    return mouvementRepository.save(mv);
  }

  @Transactional
  public MouvementStock inventaire(Long articleId, BigDecimal quantiteReelle) {
    return mouvement(new MouvementRequest(articleId, TypeMouvementStock.AJUSTEMENT, quantiteReelle, "Inventaire", "INV"));
  }

  public boolean allowNegative() {
    if (properties.stock().allowNegative()) return true;
    return parametreRepository.findById("stock.allowNegative").map(p -> "true".equalsIgnoreCase(p.getValeur())).orElse(false);
  }
}
''')
w("stock/StockController.java", '''
package com.couturix.stock;
import com.couturix.stock.dto.MouvementRequest;
import java.math.BigDecimal; import java.util.List; import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
@RestController @RequiredArgsConstructor
@PreAuthorize("hasAnyRole('SUPER_ADMIN','ADMIN','GERANT','MAGASINIER','COMPTABLE')")
public class StockController {
  private final StockService service;
  @GetMapping("/api/v1/articles-stock") public List<ArticleStock> articles() { return service.articles(); }
  @PostMapping("/api/v1/articles-stock") public ArticleStock save(@RequestBody ArticleStock a) { return service.saveArticle(a); }
  @GetMapping("/api/v1/categories-stock") public List<CategorieStock> cats() { return service.categories(); }
  @PostMapping("/api/v1/categories-stock") public CategorieStock saveCat(@RequestBody CategorieStock c) { return service.saveCategorie(c); }
  @PostMapping("/api/v1/stocks/mouvements") public MouvementStock mv(@RequestBody MouvementRequest req) { return service.mouvement(req); }
  @GetMapping("/api/v1/stocks/alertes") public List<ArticleStock> alertes() { return service.alertes(); }
  @GetMapping("/api/v1/stocks/valorisation") public Map<String, BigDecimal> valo() { return Map.of("valeur", service.valorisation()); }
  @PostMapping("/api/v1/stocks/inventaire") public MouvementStock inv(@RequestBody Map<String, Object> body) {
    return service.inventaire(Long.valueOf(body.get("articleId").toString()), new BigDecimal(body.get("quantiteReelle").toString()));
  }
}
''')

print("stock ok")
