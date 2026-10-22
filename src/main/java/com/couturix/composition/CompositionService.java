package com.couturix.composition;
import com.couturix.common.exception.ApiException;
import com.couturix.composition.dto.*;
import com.couturix.cout.CostCalculationEngine;
import com.couturix.modele.ModeleService;
import com.couturix.stock.ArticleStockRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service; import org.springframework.transaction.annotation.Transactional;
@Service @RequiredArgsConstructor
public class CompositionService {
  private final CompositionRepository repo;
  private final ModeleService modeleService;
  private final ArticleStockRepository articleRepository;

  public List<CompositionDto> byModele(Long modeleId) { return repo.findByModeleIdOrderByVersionDesc(modeleId).stream().map(this::toDto).toList(); }
  public CompositionDto get(Long id) { return toDto(require(id)); }
  public Composition require(Long id) { return repo.findById(id).orElseThrow(() -> ApiException.notFound("Composition introuvable")); }
  public Composition find(Long id) { return require(id); }

  @Transactional @PreAuthorize("hasAnyRole('SUPER_ADMIN','ADMIN','GERANT')")
  public CompositionDto create(CompositionDto dto) {
    var existing = repo.findByModeleIdOrderByVersionDesc(dto.modeleId());
    existing.stream().filter(Composition::isActive).forEach(c -> { c.setActive(false); repo.save(c); });
    Composition c = new Composition();
    c.setModele(modeleService.require(dto.modeleId()));
    c.setVersion(existing.isEmpty()?1:existing.get(0).getVersion()+1);
    c.setLibelle(dto.libelle()); c.setActive(true);
    if (dto.lignes()!=null) dto.lignes().forEach(l -> c.getLignes().add(toEntity(l, c)));
    return toDto(repo.save(c));
  }

  @Transactional @PreAuthorize("hasAnyRole('SUPER_ADMIN','ADMIN','GERANT')")
  public CompositionDto update(Long id, CompositionDto dto) {
    Composition c = require(id);
    c.setLibelle(dto.libelle());
    c.getLignes().clear();
    if (dto.lignes()!=null) dto.lignes().forEach(l -> c.getLignes().add(toEntity(l, c)));
    return toDto(repo.save(c));
  }

  public CostCalculationEngine.CostBreakdown cout(Long id) {
    Composition c = require(id);
    var lines = c.getLignes().stream().map(l -> new CostCalculationEngine.LineInput(l.getTypeLigne(), l.getQuantite(), l.getCoutUnitaire())).toList();
    return CostCalculationEngine.calculate(lines, 1);
  }

  private CompositionLigne toEntity(CompositionLigneDto l, Composition c) {
    CompositionLigne e = new CompositionLigne(); e.setComposition(c); e.setTypeLigne(l.typeLigne()); e.setDesignation(l.designation()); e.setQuantite(l.quantite()); e.setCoutUnitaire(l.coutUnitaire());
    if (l.articleId()!=null) e.setArticle(articleRepository.findById(l.articleId()).orElse(null));
    return e;
  }
  public CompositionDto toDto(Composition c) {
    return new CompositionDto(c.getId(), c.getModele().getId(), c.getVersion(), c.getLibelle(), c.isActive(),
      c.getLignes().stream().map(l -> new CompositionLigneDto(l.getId(), l.getTypeLigne(), l.getArticle()==null?null:l.getArticle().getId(), l.getDesignation(), l.getQuantite(), l.getCoutUnitaire())).toList());
  }
}
