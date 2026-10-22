package com.couturix.production;
import com.couturix.commande.Commande;
import com.couturix.domain.enums.ProductionStatut;
import com.couturix.tailleur.Tailleur;
import jakarta.persistence.*;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import lombok.Getter; import lombok.Setter;
@Getter @Setter @Entity @Table(name="production")
public class Production {
  @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
  @OneToOne(optional=false) @JoinColumn(name="commande_id") private Commande commande;
  @ManyToOne @JoinColumn(name="tailleur_id") private Tailleur tailleur;
  @Column(name="date_debut") private Instant dateDebut;
  @Column(name="date_fin") private Instant dateFin;
  @Enumerated(EnumType.STRING) @Column(nullable=false, length=30) private ProductionStatut statut = ProductionStatut.EN_ATTENTE;
  @Column(name="created_at", nullable=false) private Instant createdAt = Instant.now();
  @OneToMany(mappedBy="production", cascade=CascadeType.ALL, orphanRemoval=true) private List<ProductionEtape> etapes = new ArrayList<>();
}
