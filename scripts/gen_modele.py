# -*- coding: utf-8 -*-
"""Generate remaining CouturiX Java modules."""
from pathlib import Path
ROOT = Path(__file__).resolve().parents[1] / "src" / "main" / "java" / "com" / "couturix"

def w(rel, content):
    p = ROOT / rel
    p.parent.mkdir(parents=True, exist_ok=True)
    p.write_text(content.strip() + "\n", encoding="utf-8")

# ---------- MODELE ----------
w("modele/CategorieModele.java", r'''
package com.couturix.modele;
import jakarta.persistence.*;
import lombok.Getter; import lombok.Setter;
@Getter @Setter @Entity @Table(name="categories_modeles")
public class CategorieModele {
  @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
  @Column(nullable=false, unique=true, length=120) private String nom;
  @Column(nullable=false, unique=true, length=140) private String slug;
  @Column(columnDefinition="TEXT") private String description;
  @Column(name="image_url", length=500) private String imageUrl;
  @Column(nullable=false) private boolean actif = true;
}
''')
w("modele/Modele.java", r'''
package com.couturix.modele;
import com.couturix.common.domain.AuditableEntity;
import com.couturix.domain.enums.ModeleStatut;
import jakarta.persistence.*;
import java.math.BigDecimal; import java.util.*;
import lombok.Getter; import lombok.Setter;
@Getter @Setter @Entity @Table(name="modeles")
public class Modele extends AuditableEntity {
  @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
  @ManyToOne @JoinColumn(name="categorie_id") private CategorieModele categorie;
  @Column(nullable=false, length=180) private String nom;
  @Column(columnDefinition="TEXT") private String description;
  @Column(name="prix_indicatif", precision=14, scale=2) private BigDecimal prixIndicatif;
  @Column(name="tissu_recommande", length=180) private String tissuRecommande;
  @Column(name="temps_fabrication_jours") private Integer tempsFabricationJours;
  @Enumerated(EnumType.STRING) @Column(nullable=false, length=20) private ModeleStatut statut = ModeleStatut.BROUILLON;
  @OneToMany(mappedBy="modele", cascade=CascadeType.ALL, orphanRemoval=true) private List<ModeleImage> images = new ArrayList<>();
}
''')
w("modele/ModeleImage.java", r'''
package com.couturix.modele;
import jakarta.persistence.*;
import lombok.Getter; import lombok.Setter;
@Getter @Setter @Entity @Table(name="modele_images")
public class ModeleImage {
  @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
  @ManyToOne(optional=false) @JoinColumn(name="modele_id") private Modele modele;
  @Column(nullable=false, length=500) private String url;
  @Column(nullable=false) private int ordre;
  @Column(nullable=false) private boolean principale;
}
''')
w("modele/Favori.java", r'''
package com.couturix.modele;
import com.couturix.client.Client;
import jakarta.persistence.*;
import java.time.Instant;
import lombok.Getter; import lombok.Setter;
@Getter @Setter @Entity @Table(name="favoris")
public class Favori {
  @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
  @ManyToOne(optional=false) @JoinColumn(name="client_id") private Client client;
  @ManyToOne(optional=false) @JoinColumn(name="modele_id") private Modele modele;
  @Column(name="created_at", nullable=false) private Instant createdAt = Instant.now();
}
''')
w("modele/CategorieModeleRepository.java", "package com.couturix.modele;\nimport org.springframework.data.jpa.repository.JpaRepository;\npublic interface CategorieModeleRepository extends JpaRepository<CategorieModele, Long> {}")
w("modele/ModeleRepository.java", "package com.couturix.modele;\nimport com.couturix.domain.enums.ModeleStatut;\nimport org.springframework.data.domain.*;\nimport org.springframework.data.jpa.repository.*;\npublic interface ModeleRepository extends JpaRepository<Modele, Long>, JpaSpecificationExecutor<Modele> {\n  Page<Modele> findByStatut(ModeleStatut statut, Pageable pageable);\n}")
w("modele/ModeleImageRepository.java", "package com.couturix.modele;\nimport org.springframework.data.jpa.repository.JpaRepository;\npublic interface ModeleImageRepository extends JpaRepository<ModeleImage, Long> {}")
w("modele/FavoriRepository.java", "package com.couturix.modele;\nimport java.util.List; import java.util.Optional;\nimport org.springframework.data.jpa.repository.JpaRepository;\npublic interface FavoriRepository extends JpaRepository<Favori, Long> {\n  List<Favori> findByClientId(Long clientId);\n  Optional<Favori> findByClientIdAndModeleId(Long clientId, Long modeleId);\n  void deleteByClientIdAndModeleId(Long clientId, Long modeleId);\n}")

w("modele/dto/ModeleDto.java", '''
package com.couturix.modele.dto;
import com.couturix.domain.enums.ModeleStatut;
import java.math.BigDecimal; import java.util.List;
public record ModeleDto(Long id, Long categorieId, String categorieNom, String nom, String description, BigDecimal prixIndicatif, String tissuRecommande, Integer tempsFabricationJours, ModeleStatut statut, List<String> images) {}
''')
w("modele/dto/ModeleRequest.java", '''
package com.couturix.modele.dto;
import jakarta.validation.constraints.NotBlank;
import java.math.BigDecimal;
public record ModeleRequest(@NotBlank String nom, Long categorieId, String description, BigDecimal prixIndicatif, String tissuRecommande, Integer tempsFabricationJours) {}
''')
w("modele/dto/CategorieDto.java", '''
package com.couturix.modele.dto;
public record CategorieDto(Long id, String nom, String slug, String description, String imageUrl, boolean actif) {}
''')

w("modele/ModeleService.java", r'''
package com.couturix.modele;
import com.couturix.common.api.PageResponse;
import com.couturix.common.exception.ApiException;
import com.couturix.domain.enums.ModeleStatut;
import com.couturix.modele.dto.*;
import com.couturix.storage.FileStorageService;
import java.text.Normalizer; import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service; import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service @RequiredArgsConstructor
public class ModeleService {
  private final ModeleRepository modeleRepository;
  private final CategorieModeleRepository categorieRepository;
  private final FileStorageService fileStorageService;

  @Transactional(readOnly=true)
  public PageResponse<ModeleDto> list(ModeleStatut statut, String q, Pageable pageable) {
    var spec = (org.springframework.data.jpa.domain.Specification<Modele>) (root, query, cb) -> {
      var p = cb.conjunction();
      if (statut != null) p = cb.and(p, cb.equal(root.get("statut"), statut));
      if (q != null && !q.isBlank()) p = cb.and(p, cb.like(cb.lower(root.get("nom")), "%" + q.toLowerCase() + "%"));
      return p;
    };
    return PageResponse.of(modeleRepository.findAll(spec, pageable).map(this::toDto));
  }

  @Transactional(readOnly=true) public ModeleDto get(Long id) { return toDto(require(id)); }

  @Transactional @PreAuthorize("hasAnyRole('SUPER_ADMIN','ADMIN','GERANT')")
  public ModeleDto create(ModeleRequest req) {
    Modele m = new Modele(); apply(m, req); m.setStatut(ModeleStatut.BROUILLON);
    return toDto(modeleRepository.save(m));
  }

  @Transactional @PreAuthorize("hasAnyRole('SUPER_ADMIN','ADMIN','GERANT')")
  public ModeleDto update(Long id, ModeleRequest req) { Modele m = require(id); apply(m, req); return toDto(modeleRepository.save(m)); }

  @Transactional @PreAuthorize("hasAnyRole('SUPER_ADMIN','ADMIN','GERANT')")
  public ModeleDto changerStatut(Long id, ModeleStatut statut) { Modele m = require(id); m.setStatut(statut); return toDto(modeleRepository.save(m)); }

  @Transactional @PreAuthorize("hasAnyRole('SUPER_ADMIN','ADMIN','GERANT')")
  public ModeleDto addImage(Long id, MultipartFile file, boolean principale) {
    Modele m = require(id);
    ModeleImage img = new ModeleImage(); img.setModele(m); img.setUrl(fileStorageService.store(file, "modeles"));
    img.setPrincipale(principale); img.setOrdre(m.getImages().size());
    m.getImages().add(img); return toDto(modeleRepository.save(m));
  }

  public List<CategorieDto> categories() {
    return categorieRepository.findAll().stream().map(c -> new CategorieDto(c.getId(), c.getNom(), c.getSlug(), c.getDescription(), c.getImageUrl(), c.isActif())).toList();
  }

  @Transactional @PreAuthorize("hasAnyRole('SUPER_ADMIN','ADMIN','GERANT')")
  public CategorieDto saveCategorie(CategorieDto dto) {
    CategorieModele c = dto.id()==null ? new CategorieModele() : categorieRepository.findById(dto.id()).orElseThrow(() -> ApiException.notFound("Catégorie introuvable"));
    c.setNom(dto.nom()); c.setSlug(slugify(dto.nom())); c.setDescription(dto.description()); c.setActif(dto.actif());
    CategorieModele saved = categorieRepository.save(c);
    return new CategorieDto(saved.getId(), saved.getNom(), saved.getSlug(), saved.getDescription(), saved.getImageUrl(), saved.isActif());
  }

  public Modele require(Long id) { return modeleRepository.findById(id).orElseThrow(() -> ApiException.notFound("Modèle introuvable")); }

  private void apply(Modele m, ModeleRequest req) {
    m.setNom(req.nom()); m.setDescription(req.description()); m.setPrixIndicatif(req.prixIndicatif());
    m.setTissuRecommande(req.tissuRecommande()); m.setTempsFabricationJours(req.tempsFabricationJours());
    if (req.categorieId()!=null) m.setCategorie(categorieRepository.findById(req.categorieId()).orElseThrow(() -> ApiException.notFound("Catégorie introuvable")));
  }

  public ModeleDto toDto(Modele m) {
    return new ModeleDto(m.getId(), m.getCategorie()==null?null:m.getCategorie().getId(), m.getCategorie()==null?null:m.getCategorie().getNom(),
      m.getNom(), m.getDescription(), m.getPrixIndicatif(), m.getTissuRecommande(), m.getTempsFabricationJours(), m.getStatut(),
      m.getImages().stream().map(ModeleImage::getUrl).toList());
  }

  private String slugify(String nom) {
    String n = Normalizer.normalize(nom, Normalizer.Form.NFD).replaceAll("\\p{M}", "").toLowerCase().replaceAll("[^a-z0-9]+","-");
    return n.replaceAll("^-|-$", "");
  }
}
''')

w("modele/ModeleController.java", r'''
package com.couturix.modele;
import com.couturix.common.api.PageResponse;
import com.couturix.domain.enums.ModeleStatut;
import com.couturix.modele.dto.*;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController @RequestMapping("/api/v1/modeles") @RequiredArgsConstructor @Tag(name="Modèles")
@PreAuthorize("hasAnyRole('SUPER_ADMIN','ADMIN','GERANT','RECEPTIONNISTE')")
public class ModeleController {
  private final ModeleService service;
  @GetMapping public PageResponse<ModeleDto> list(@RequestParam(required=false) ModeleStatut statut, @RequestParam(required=false) String q, Pageable pageable) { return service.list(statut,q,pageable); }
  @GetMapping("/{id}") public ModeleDto get(@PathVariable Long id) { return service.get(id); }
  @PostMapping public ModeleDto create(@Valid @RequestBody ModeleRequest req) { return service.create(req); }
  @PutMapping("/{id}") public ModeleDto update(@PathVariable Long id, @Valid @RequestBody ModeleRequest req) { return service.update(id, req); }
  @PostMapping("/{id}/publier") public ModeleDto publier(@PathVariable Long id) { return service.changerStatut(id, ModeleStatut.PUBLIE); }
  @PostMapping("/{id}/depublier") public ModeleDto depublier(@PathVariable Long id) { return service.changerStatut(id, ModeleStatut.DEPUBLIE); }
  @PostMapping("/{id}/archiver") public ModeleDto archiver(@PathVariable Long id) { return service.changerStatut(id, ModeleStatut.ARCHIVE); }
  @PostMapping("/{id}/images") public ModeleDto image(@PathVariable Long id, @RequestParam MultipartFile file, @RequestParam(defaultValue="false") boolean principale) { return service.addImage(id, file, principale); }
}
''')

w("modele/CategorieModeleController.java", r'''
package com.couturix.modele;
import com.couturix.modele.dto.CategorieDto;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
@RestController @RequestMapping("/api/v1/categories-modeles") @RequiredArgsConstructor
@PreAuthorize("isAuthenticated()")
public class CategorieModeleController {
  private final ModeleService service;
  @GetMapping public List<CategorieDto> list() { return service.categories(); }
  @PostMapping @PreAuthorize("hasAnyRole('SUPER_ADMIN','ADMIN','GERANT')") public CategorieDto save(@RequestBody CategorieDto dto) { return service.saveCategorie(dto); }
}
''')

w("modele/PublicModeleController.java", r'''
package com.couturix.modele;
import com.couturix.client.Client;
import com.couturix.client.ClientRepository;
import com.couturix.client.dto.ClientRequest;
import com.couturix.client.ClientService;
import com.couturix.commande.CommandeService;
import com.couturix.common.api.PageResponse;
import com.couturix.domain.enums.ModeleStatut;
import com.couturix.modele.dto.CategorieDto;
import com.couturix.modele.dto.ModeleDto;
import java.util.List; import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;

@RestController @RequestMapping("/api/v1/public") @RequiredArgsConstructor
public class PublicModeleController {
  private final ModeleService modeleService;
  private final ClientService clientService;
  private final ClientRepository clientRepository;
  private final CommandeService commandeService;

  @GetMapping("/modeles") public PageResponse<ModeleDto> modeles(@RequestParam(required=false) String q, Pageable pageable) {
    return modeleService.list(ModeleStatut.PUBLIE, q, pageable);
  }
  @GetMapping("/modeles/{id}") public ModeleDto modele(@PathVariable Long id) { return modeleService.get(id); }
  @GetMapping("/categories") public List<CategorieDto> categories() { return modeleService.categories(); }

  @PostMapping("/demandes")
  public Map<String, Object> demande(@RequestBody Map<String, Object> body) {
    String nom = String.valueOf(body.getOrDefault("nom",""));
    String prenom = String.valueOf(body.getOrDefault("prenom",""));
    String telephone = String.valueOf(body.getOrDefault("telephone",""));
    Long modeleId = Long.valueOf(body.get("modeleId").toString());
    Client client = clientRepository.findAll().stream().filter(c -> telephone.equals(c.getTelephone())).findFirst()
        .orElseGet(() -> { var dto = clientService.create(new ClientRequest(nom, prenom, telephone, null, null, "Demande catalogue")); return clientService.require(dto.id()); });
    var cmd = commandeService.createFromPublic(client.getId(), modeleId, body.get("notes")==null?null:body.get("notes").toString());
    return Map.of("commandeId", cmd.id(), "numero", cmd.numero());
  }
}
''')

print("modele written")
