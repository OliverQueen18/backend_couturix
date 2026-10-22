# -*- coding: utf-8 -*-
from pathlib import Path
ROOT = Path(__file__).resolve().parents[1] / "src" / "main" / "java" / "com" / "couturix"

def w(rel, content):
    p = ROOT / rel
    p.parent.mkdir(parents=True, exist_ok=True)
    p.write_text(content.strip() + "\n", encoding="utf-8")

w("composition/Composition.java", '''
package com.couturix.composition;
import com.couturix.modele.Modele;
import jakarta.persistence.*;
import java.time.Instant; import java.util.*;
import lombok.Getter; import lombok.Setter;
@Getter @Setter @Entity @Table(name="compositions")
public class Composition {
  @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
  @ManyToOne(optional=false) @JoinColumn(name="modele_id") private Modele modele;
  @Column(nullable=false) private int version;
  private String libelle;
  @Column(nullable=false) private boolean active = true;
  @Column(name="created_at", nullable=false) private Instant createdAt = Instant.now();
  @OneToMany(mappedBy="composition", cascade=CascadeType.ALL, orphanRemoval=true) private List<CompositionLigne> lignes = new ArrayList<>();
}
''')
w("composition/CompositionLigne.java", '''
package com.couturix.composition;
import com.couturix.domain.enums.TypeLigneComposition;
import com.couturix.stock.ArticleStock;
import jakarta.persistence.*;
import java.math.BigDecimal;
import lombok.Getter; import lombok.Setter;
@Getter @Setter @Entity @Table(name="composition_lignes")
public class CompositionLigne {
  @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
  @ManyToOne(optional=false) @JoinColumn(name="composition_id") private Composition composition;
  @Enumerated(EnumType.STRING) @Column(name="type_ligne", nullable=false, length=40) private TypeLigneComposition typeLigne;
  @ManyToOne @JoinColumn(name="article_id") private ArticleStock article;
  @Column(nullable=false, length=180) private String designation;
  @Column(nullable=false, precision=14, scale=3) private BigDecimal quantite;
  @Column(name="cout_unitaire", nullable=false, precision=14, scale=2) private BigDecimal coutUnitaire = BigDecimal.ZERO;
}
''')
w("composition/CompositionRepository.java", "package com.couturix.composition;\nimport java.util.List; import java.util.Optional;\nimport org.springframework.data.jpa.repository.JpaRepository;\npublic interface CompositionRepository extends JpaRepository<Composition, Long> {\n  List<Composition> findByModeleIdOrderByVersionDesc(Long modeleId);\n  Optional<Composition> findFirstByModeleIdAndActiveTrue(Long modeleId);\n}")
w("composition/dto/CompositionLigneDto.java", '''
package com.couturix.composition.dto;
import com.couturix.domain.enums.TypeLigneComposition;
import java.math.BigDecimal;
public record CompositionLigneDto(Long id, TypeLigneComposition typeLigne, Long articleId, String designation, BigDecimal quantite, BigDecimal coutUnitaire) {}
''')
w("composition/dto/CompositionDto.java", '''
package com.couturix.composition.dto;
import java.util.List;
public record CompositionDto(Long id, Long modeleId, int version, String libelle, boolean active, List<CompositionLigneDto> lignes) {}
''')
w("composition/CompositionService.java", '''
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
''')
w("composition/CompositionController.java", '''
package com.couturix.composition;
import com.couturix.composition.dto.CompositionDto;
import com.couturix.cout.CostCalculationEngine;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
@RestController @RequestMapping("/api/v1/compositions") @RequiredArgsConstructor
@PreAuthorize("hasAnyRole('SUPER_ADMIN','ADMIN','GERANT','COMPTABLE')")
public class CompositionController {
  private final CompositionService service;
  @GetMapping("/modele/{modeleId}") public List<CompositionDto> byModele(@PathVariable Long modeleId) { return service.byModele(modeleId); }
  @GetMapping("/{id}") public CompositionDto get(@PathVariable Long id) { return service.get(id); }
  @PostMapping public CompositionDto create(@RequestBody CompositionDto dto) { return service.create(dto); }
  @GetMapping("/{id}/cout") public CostCalculationEngine.CostBreakdown cout(@PathVariable Long id) { return service.cout(id); }
}
''')

print("composition ok")
