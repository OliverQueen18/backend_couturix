package com.couturix.tailleur;
import com.couturix.atelier.Atelier;
import com.couturix.common.tenant.TenantAssigner;
import com.couturix.common.tenant.TenantOwned;
import com.couturix.domain.enums.ModeRemuneration;
import com.couturix.user.User;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import java.math.BigDecimal; import java.time.Instant; import java.util.*;
import lombok.Getter; import lombok.Setter;
import org.hibernate.annotations.Filter;
@Getter @Setter @Entity @Table(name="tailleurs")
@Filter(name = "atelierFilter", condition = "atelier_id IN (:atelierIds)")
@EntityListeners(TenantAssigner.class)
public class Tailleur implements TenantOwned {
  @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
  @JsonIgnore
  @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "atelier_id") private Atelier atelier;
  @JsonIgnore
  @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name="user_id") private User user;
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
  @ManyToMany(fetch = FetchType.LAZY)
  @JoinTable(name="tailleur_specialites", joinColumns=@JoinColumn(name="tailleur_id"), inverseJoinColumns=@JoinColumn(name="specialite_id"))
  private Set<Specialite> specialites = new HashSet<>();
  public String nomComplet() { return prenom + " " + nom; }
}
