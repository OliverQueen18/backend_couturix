package com.couturix.stock;
import com.couturix.atelier.Atelier;
import com.couturix.common.tenant.TenantAssigner;
import com.couturix.common.tenant.TenantOwned;
import com.couturix.fournisseur.Fournisseur;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import java.math.BigDecimal; import java.time.Instant;
import lombok.Getter; import lombok.Setter;
import org.hibernate.annotations.Filter;
@Getter @Setter @Entity @Table(name="articles_stock")
@Filter(name = "atelierFilter", condition = "atelier_id IN (:atelierIds)")
@EntityListeners(TenantAssigner.class)
public class ArticleStock implements TenantOwned {
  @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
  @JsonIgnore
  @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "atelier_id") private Atelier atelier;
  @ManyToOne @JoinColumn(name="categorie_id") private CategorieStock categorie;
  @ManyToOne @JoinColumn(name="fournisseur_id") private Fournisseur fournisseur;
  @Column(nullable=false, length=180) private String nom;
  @Column(nullable=false, unique=true, length=60) private String sku;
  @Column(nullable=false, length=30) private String unite = "UNITE";
  @Column(name="stock_actuel", nullable=false, precision=14, scale=3) private BigDecimal stockActuel = BigDecimal.ZERO;
  @Column(name="stock_minimum", nullable=false, precision=14, scale=3) private BigDecimal stockMinimum = BigDecimal.ZERO;
  @Column(name="prix_achat", nullable=false, precision=14, scale=2) private BigDecimal prixAchat = BigDecimal.ZERO;
  @Column(name="prix_vente", precision=14, scale=2) private BigDecimal prixVente;
  @Column(nullable=false) private boolean actif = true;
  @Column(name="created_at", nullable=false) private Instant createdAt = Instant.now();
  @Column(name="updated_at", nullable=false) private Instant updatedAt = Instant.now();
}
