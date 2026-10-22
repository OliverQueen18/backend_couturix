package com.couturix.modele;
import com.couturix.common.domain.TenantEntity;
import com.couturix.domain.enums.ModeleStatut;
import jakarta.persistence.*;
import java.math.BigDecimal; import java.util.*;
import lombok.Getter; import lombok.Setter;
@Getter @Setter @Entity @Table(name="modeles")
public class Modele extends TenantEntity {
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
