package com.couturix.caisse;
import com.couturix.atelier.Atelier;
import com.couturix.common.tenant.TenantAssigner;
import com.couturix.common.tenant.TenantOwned;
import com.couturix.domain.enums.CaisseStatut;
import com.couturix.user.User;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import java.math.BigDecimal; import java.time.Instant; import java.util.*;
import lombok.Getter; import lombok.Setter;
import org.hibernate.annotations.Filter;
@Getter @Setter @Entity @Table(name="caisse")
@Filter(name = "atelierFilter", condition = "atelier_id IN (:atelierIds)")
@EntityListeners(TenantAssigner.class)
public class Caisse implements TenantOwned {
  @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
  @JsonIgnore
  @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "atelier_id") private Atelier atelier;
  @Column(name="date_ouverture", nullable=false) private Instant dateOuverture = Instant.now();
  @Column(name="date_cloture") private Instant dateCloture;
  @Column(name="solde_initial", nullable=false, precision=14, scale=2) private BigDecimal soldeInitial = BigDecimal.ZERO;
  @Column(name="solde_theorique", precision=14, scale=2) private BigDecimal soldeTheorique;
  @Column(name="solde_reel", precision=14, scale=2) private BigDecimal soldeReel;
  @Enumerated(EnumType.STRING) @Column(nullable=false, length=20) private CaisseStatut statut = CaisseStatut.OUVERTE;
  @JsonIgnore
  @ManyToOne @JoinColumn(name="user_id") private User user;
  private String notes;
  @OneToMany(mappedBy="caisse", fetch = FetchType.EAGER) private List<CaisseOperation> operations = new ArrayList<>();
}
