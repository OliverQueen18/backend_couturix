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
