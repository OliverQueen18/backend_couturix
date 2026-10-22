package com.couturix.fournisseur;
import com.couturix.atelier.Atelier;
import com.couturix.common.tenant.TenantAssigner;
import com.couturix.common.tenant.TenantOwned;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import java.time.Instant;
import lombok.Getter; import lombok.Setter;
import org.hibernate.annotations.Filter;
@Getter @Setter @Entity @Table(name="fournisseurs")
@Filter(name = "atelierFilter", condition = "atelier_id IN (:atelierIds)")
@EntityListeners(TenantAssigner.class)
public class Fournisseur implements TenantOwned {
  @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
  @JsonIgnore
  @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "atelier_id") private Atelier atelier;
  @Column(nullable=false, length=180) private String nom;
  private String telephone; private String email; private String adresse;
  @Column(columnDefinition="TEXT") private String notes;
  @Column(nullable=false) private boolean actif = true;
  @Column(name="created_at", nullable=false) private Instant createdAt = Instant.now();
  @Column(name="updated_at", nullable=false) private Instant updatedAt = Instant.now();
}
