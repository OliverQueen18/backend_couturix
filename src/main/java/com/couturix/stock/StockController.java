package com.couturix.stock;
import com.couturix.stock.dto.InventaireBatchRequest;
import com.couturix.stock.dto.InventaireResultatDto;
import com.couturix.stock.dto.MouvementDto;
import com.couturix.stock.dto.MouvementRequest;
import jakarta.validation.Valid;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController @RequiredArgsConstructor
@PreAuthorize("hasAnyRole('SUPER_ADMIN','ADMIN','GERANT','MAGASINIER','COMPTABLE')")
public class StockController {
  private final StockService service;

  @GetMapping("/api/v1/articles-stock") public List<ArticleStock> articles() { return service.articles(); }
  @PostMapping("/api/v1/articles-stock") public ArticleStock save(@RequestBody ArticleStock a) { return service.saveArticle(a); }
  @PutMapping("/api/v1/articles-stock/{id}") public ArticleStock update(@PathVariable Long id, @RequestBody ArticleStock a) {
    a.setId(id);
    return service.saveArticle(a);
  }
  @GetMapping("/api/v1/categories-stock") public List<CategorieStock> cats() { return service.categories(); }
  @PostMapping("/api/v1/categories-stock") public CategorieStock saveCat(@RequestBody CategorieStock c) { return service.saveCategorie(c); }
  @PostMapping("/api/v1/stocks/mouvements") public MouvementStock mv(@Valid @RequestBody MouvementRequest req) { return service.mouvement(req); }
  @GetMapping("/api/v1/stocks/mouvements") public List<MouvementDto> mouvements(@RequestParam(required = false) Long articleId) {
    return service.mouvements(articleId);
  }
  @GetMapping("/api/v1/stocks/alertes") public List<ArticleStock> alertes() { return service.alertes(); }
  @GetMapping("/api/v1/stocks/valorisation") public Map<String, BigDecimal> valo() { return Map.of("valeur", service.valorisation()); }
  @PostMapping("/api/v1/stocks/inventaire")
  @PreAuthorize("hasAnyRole('SUPER_ADMIN','ADMIN','GERANT','MAGASINIER')")
  public InventaireResultatDto inv(@Valid @RequestBody InventaireBatchRequest body) {
    return service.inventaire(body.lignes());
  }
}
