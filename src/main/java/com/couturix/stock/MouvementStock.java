package com.couturix.stock;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.couturix.domain.enums.TypeMouvementStock;
import com.couturix.user.User;
import jakarta.persistence.*;
import java.math.BigDecimal; import java.time.Instant;
import lombok.Getter; import lombok.Setter;
@Getter @Setter @Entity @Table(name="mouvements_stock")
public class MouvementStock {
  @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
  @JsonIgnoreProperties({"atelier", "categorie", "fournisseur"})
  @ManyToOne(optional=false) @JoinColumn(name="article_id") private ArticleStock article;
  @Enumerated(EnumType.STRING) @Column(name="type_mouvement", nullable=false, length=40) private TypeMouvementStock typeMouvement;
  @Column(nullable=false, precision=14, scale=3) private BigDecimal quantite;
  @Column(name="stock_avant", nullable=false, precision=14, scale=3) private BigDecimal stockAvant;
  @Column(name="stock_apres", nullable=false, precision=14, scale=3) private BigDecimal stockApres;
  private String motif;
  @Column(name="reference_metier", length=80) private String referenceMetier;
  @JsonIgnoreProperties({"passwordHash", "roles", "atelier"})
  @ManyToOne @JoinColumn(name="user_id") private User user;
  @Column(name="created_at", nullable=false) private Instant createdAt = Instant.now();
}
