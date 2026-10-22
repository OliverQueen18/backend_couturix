# -*- coding: utf-8 -*-
from pathlib import Path
ROOT = Path(__file__).resolve().parents[1] / "src" / "main" / "java" / "com" / "couturix"

def w(rel, content):
    p = ROOT / rel
    p.parent.mkdir(parents=True, exist_ok=True)
    p.write_text(content.strip() + "\n", encoding="utf-8")

# TAILLEUR
w("tailleur/Specialite.java", '''
package com.couturix.tailleur;
import jakarta.persistence.*;
import lombok.Getter; import lombok.Setter;
@Getter @Setter @Entity @Table(name="specialites")
public class Specialite {
  @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
  @Column(nullable=false, unique=true, length=120) private String libelle;
}
''')
w("tailleur/Tailleur.java", '''
package com.couturix.tailleur;
import com.couturix.domain.enums.ModeRemuneration;
import com.couturix.user.User;
import jakarta.persistence.*;
import java.math.BigDecimal; import java.time.Instant; import java.util.*;
import lombok.Getter; import lombok.Setter;
@Getter @Setter @Entity @Table(name="tailleurs")
public class Tailleur {
  @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
  @ManyToOne @JoinColumn(name="user_id") private User user;
  @Column(nullable=false, length=100) private String nom;
  @Column(nullable=false, length=100) private String prenom;
  private String telephone; private String email;
  @Enumerated(EnumType.STRING) @Column(name="mode_remuneration", nullable=false, length=30) private ModeRemuneration modeRemuneration = ModeRemuneration.FIXE;
  @Column(name="salaire_fixe", precision=14, scale=2) private BigDecimal salaireFixe = BigDecimal.ZERO;
  @Column(name="taux_piece", precision=14, scale=2) private BigDecimal tauxPiece = BigDecimal.ZERO;
  @Column(precision=6, scale=3) private BigDecimal pourcentage = BigDecimal.ZERO;
  @Column(nullable=false) private boolean actif = true;
  @Column(name="created_at", nullable=false) private Instant createdAt = Instant.now();
  @Column(name="updated_at", nullable=false) private Instant updatedAt = Instant.now();
  @ManyToMany @JoinTable(name="tailleur_specialites", joinColumns=@JoinColumn(name="tailleur_id"), inverseJoinColumns=@JoinColumn(name="specialite_id"))
  private Set<Specialite> specialites = new HashSet<>();
  public String nomComplet() { return prenom + " " + nom; }
}
''')
w("tailleur/SpecialiteRepository.java", "package com.couturix.tailleur;\nimport org.springframework.data.jpa.repository.JpaRepository;\npublic interface SpecialiteRepository extends JpaRepository<Specialite, Long> {}")
w("tailleur/TailleurRepository.java", "package com.couturix.tailleur;\nimport org.springframework.data.jpa.repository.JpaRepository;\npublic interface TailleurRepository extends JpaRepository<Tailleur, Long> {}")
w("tailleur/dto/TailleurDto.java", '''
package com.couturix.tailleur.dto;
import com.couturix.domain.enums.ModeRemuneration;
import java.math.BigDecimal; import java.util.Set;
public record TailleurDto(Long id, String nom, String prenom, String telephone, String email, ModeRemuneration modeRemuneration, BigDecimal salaireFixe, BigDecimal tauxPiece, BigDecimal pourcentage, boolean actif, Set<String> specialites) {}
''')
w("tailleur/TailleurService.java", '''
package com.couturix.tailleur;
import com.couturix.common.exception.ApiException;
import com.couturix.tailleur.dto.TailleurDto;
import java.util.List; import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service; import org.springframework.transaction.annotation.Transactional;
@Service @RequiredArgsConstructor
public class TailleurService {
  private final TailleurRepository repo; private final SpecialiteRepository specialiteRepository;
  public List<TailleurDto> list() { return repo.findAll().stream().map(this::toDto).toList(); }
  public TailleurDto get(Long id) { return toDto(require(id)); }
  public Tailleur require(Long id) { return repo.findById(id).orElseThrow(() -> ApiException.notFound("Tailleur introuvable")); }
  @Transactional @PreAuthorize("hasAnyRole('SUPER_ADMIN','ADMIN','GERANT')")
  public TailleurDto save(TailleurDto dto) {
    Tailleur t = dto.id()==null ? new Tailleur() : require(dto.id());
    t.setNom(dto.nom()); t.setPrenom(dto.prenom()); t.setTelephone(dto.telephone()); t.setEmail(dto.email());
    t.setModeRemuneration(dto.modeRemuneration()); t.setSalaireFixe(dto.salaireFixe()); t.setTauxPiece(dto.tauxPiece()); t.setPourcentage(dto.pourcentage()); t.setActif(dto.actif());
    if (dto.specialites()!=null) t.setSpecialites(dto.specialites().stream().map(n -> specialiteRepository.findAll().stream().filter(s -> s.getLibelle().equals(n)).findFirst().orElseGet(() -> { Specialite s=new Specialite(); s.setLibelle(n); return specialiteRepository.save(s); })).collect(Collectors.toSet()));
    return toDto(repo.save(t));
  }
  public List<Specialite> specialites() { return specialiteRepository.findAll(); }
  public TailleurDto toDto(Tailleur t) {
    return new TailleurDto(t.getId(), t.getNom(), t.getPrenom(), t.getTelephone(), t.getEmail(), t.getModeRemuneration(), t.getSalaireFixe(), t.getTauxPiece(), t.getPourcentage(), t.isActif(), t.getSpecialites().stream().map(Specialite::getLibelle).collect(Collectors.toSet()));
  }
}
''')
w("tailleur/TailleurController.java", '''
package com.couturix.tailleur;
import com.couturix.tailleur.dto.TailleurDto;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
@RestController @RequestMapping("/api/v1") @RequiredArgsConstructor
@PreAuthorize("hasAnyRole('SUPER_ADMIN','ADMIN','GERANT','COMPTABLE','RECEPTIONNISTE')")
public class TailleurController {
  private final TailleurService service;
  @GetMapping("/tailleurs") public List<TailleurDto> list() { return service.list(); }
  @GetMapping("/tailleurs/{id}") public TailleurDto get(@PathVariable Long id) { return service.get(id); }
  @PostMapping("/tailleurs") public TailleurDto create(@RequestBody TailleurDto dto) { return service.save(dto); }
  @PutMapping("/tailleurs/{id}") public TailleurDto update(@PathVariable Long id, @RequestBody TailleurDto dto) { return service.save(new TailleurDto(id, dto.nom(), dto.prenom(), dto.telephone(), dto.email(), dto.modeRemuneration(), dto.salaireFixe(), dto.tauxPiece(), dto.pourcentage(), dto.actif(), dto.specialites())); }
  @GetMapping("/specialites") public List<Specialite> specialites() { return service.specialites(); }
}
''')

# FOURNISSEUR + STOCK
w("fournisseur/Fournisseur.java", '''
package com.couturix.fournisseur;
import jakarta.persistence.*;
import java.time.Instant;
import lombok.Getter; import lombok.Setter;
@Getter @Setter @Entity @Table(name="fournisseurs")
public class Fournisseur {
  @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
  @Column(nullable=false, length=180) private String nom;
  private String telephone; private String email; private String adresse;
  @Column(columnDefinition="TEXT") private String notes;
  @Column(nullable=false) private boolean actif = true;
  @Column(name="created_at", nullable=false) private Instant createdAt = Instant.now();
  @Column(name="updated_at", nullable=false) private Instant updatedAt = Instant.now();
}
''')
w("fournisseur/FournisseurRepository.java", "package com.couturix.fournisseur;\nimport org.springframework.data.jpa.repository.JpaRepository;\npublic interface FournisseurRepository extends JpaRepository<Fournisseur, Long> {}")
w("fournisseur/FournisseurController.java", '''
package com.couturix.fournisseur;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
@RestController @RequestMapping("/api/v1/fournisseurs") @RequiredArgsConstructor
@PreAuthorize("hasAnyRole('SUPER_ADMIN','ADMIN','GERANT','MAGASINIER','COMPTABLE')")
public class FournisseurController {
  private final FournisseurRepository repo;
  @GetMapping public List<Fournisseur> list() { return repo.findAll(); }
  @PostMapping public Fournisseur create(@RequestBody Fournisseur f) { return repo.save(f); }
  @PutMapping("/{id}") public Fournisseur update(@PathVariable Long id, @RequestBody Fournisseur f) { f.setId(id); return repo.save(f); }
}
''')

print("tailleur+fournisseur ok")
