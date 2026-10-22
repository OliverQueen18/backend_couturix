# -*- coding: utf-8 -*-
from pathlib import Path
ROOT = Path(__file__).resolve().parents[1] / "src" / "main" / "java" / "com" / "couturix"

def w(rel, content):
    p = ROOT / rel
    p.parent.mkdir(parents=True, exist_ok=True)
    p.write_text(content.strip() + "\n", encoding="utf-8")

w("commande/Commande.java", '''
package com.couturix.commande;
import com.couturix.client.Client;
import com.couturix.common.domain.AuditableEntity;
import com.couturix.composition.Composition;
import com.couturix.domain.enums.CommandeStatut;
import com.couturix.domain.enums.Priorite;
import com.couturix.modele.Modele;
import com.couturix.tailleur.Tailleur;
import jakarta.persistence.*;
import java.math.BigDecimal; import java.time.LocalDate; import java.util.*;
import lombok.Getter; import lombok.Setter;
@Getter @Setter @Entity @Table(name="commandes")
public class Commande extends AuditableEntity {
  @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
  @Column(nullable=false, unique=true, length=40) private String numero;
  @ManyToOne(optional=false) @JoinColumn(name="client_id") private Client client;
  @ManyToOne @JoinColumn(name="modele_id") private Modele modele;
  @ManyToOne @JoinColumn(name="composition_id") private Composition composition;
  @Column(name="devis_id") private Long devisId;
  @ManyToOne @JoinColumn(name="tailleur_id") private Tailleur tailleur;
  @Enumerated(EnumType.STRING) @Column(nullable=false, length=30) private CommandeStatut statut = CommandeStatut.BROUILLON;
  @Enumerated(EnumType.STRING) @Column(nullable=false, length=20) private Priorite priorite = Priorite.NORMALE;
  @Column(name="date_prevue") private LocalDate datePrevue;
  @Column(name="date_livraison") private LocalDate dateLivraison;
  @Column(nullable=false) private int quantite = 1;
  @Column(name="prix_unitaire", nullable=false, precision=14, scale=2) private BigDecimal prixUnitaire = BigDecimal.ZERO;
  @Column(nullable=false, precision=14, scale=2) private BigDecimal remise = BigDecimal.ZERO;
  @Column(name="montant_total", nullable=false, precision=14, scale=2) private BigDecimal montantTotal = BigDecimal.ZERO;
  @Column(columnDefinition="TEXT") private String notes;
  @OneToMany(mappedBy="commande", cascade=CascadeType.ALL, orphanRemoval=true) private List<CommandeDetail> details = new ArrayList<>();
}
''')
w("commande/CommandeDetail.java", '''
package com.couturix.commande;
import jakarta.persistence.*;
import java.math.BigDecimal;
import lombok.Getter; import lombok.Setter;
@Getter @Setter @Entity @Table(name="commande_details")
public class CommandeDetail {
  @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
  @ManyToOne(optional=false) @JoinColumn(name="commande_id") private Commande commande;
  @Column(nullable=false) private String designation;
  @Column(nullable=false) private int quantite = 1;
  @Column(name="prix_unitaire", nullable=false, precision=14, scale=2) private BigDecimal prixUnitaire = BigDecimal.ZERO;
}
''')
w("commande/CommandeMesure.java", '''
package com.couturix.commande;
import jakarta.persistence.*;
import java.math.BigDecimal;
import lombok.Getter; import lombok.Setter;
@Getter @Setter @Entity @Table(name="commande_mesures")
public class CommandeMesure {
  @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
  @OneToOne(optional=false) @JoinColumn(name="commande_id") private Commande commande;
  @Column(name="tour_poitrine", precision=8, scale=2) private BigDecimal tourPoitrine;
  @Column(name="tour_taille", precision=8, scale=2) private BigDecimal tourTaille;
  @Column(name="tour_hanches", precision=8, scale=2) private BigDecimal tourHanches;
  @Column(name="longueur_manche", precision=8, scale=2) private BigDecimal longueurManche;
  @Column(name="longueur_pantalon", precision=8, scale=2) private BigDecimal longueurPantalon;
  @Column(columnDefinition="TEXT") private String notes;
}
''')
w("commande/CommandeStatutHistorique.java", '''
package com.couturix.commande;
import com.couturix.domain.enums.CommandeStatut;
import com.couturix.user.User;
import jakarta.persistence.*;
import java.time.Instant;
import lombok.Getter; import lombok.Setter;
@Getter @Setter @Entity @Table(name="commande_statuts_historique")
public class CommandeStatutHistorique {
  @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
  @ManyToOne(optional=false) @JoinColumn(name="commande_id") private Commande commande;
  @Enumerated(EnumType.STRING) @Column(length=30) private CommandeStatut ancien;
  @Enumerated(EnumType.STRING) @Column(nullable=false, length=30) private CommandeStatut nouveau;
  private String commentaire;
  @ManyToOne @JoinColumn(name="user_id") private User user;
  @Column(name="created_at", nullable=false) private Instant createdAt = Instant.now();
}
''')
w("commande/CommandeRepository.java", '''
package com.couturix.commande;
import com.couturix.domain.enums.CommandeStatut;
import java.time.LocalDate; import java.util.List;
import org.springframework.data.jpa.repository.*;
public interface CommandeRepository extends JpaRepository<Commande, Long>, JpaSpecificationExecutor<Commande> {
  long countByNumeroStartingWith(String prefix);
  List<Commande> findByClientIdOrderByCreatedAtDesc(Long clientId);
  long countByDatePrevueBeforeAndStatutNot(LocalDate date, CommandeStatut statut);
}
''')
w("commande/CommandeStatutHistoriqueRepository.java", "package com.couturix.commande;\nimport org.springframework.data.jpa.repository.JpaRepository;\npublic interface CommandeStatutHistoriqueRepository extends JpaRepository<CommandeStatutHistorique, Long> {}")
w("commande/CommandeMesureRepository.java", "package com.couturix.commande;\nimport org.springframework.data.jpa.repository.JpaRepository;\npublic interface CommandeMesureRepository extends JpaRepository<CommandeMesure, Long> {}")
w("commande/dto/CommandeDto.java", '''
package com.couturix.commande.dto;
import com.couturix.domain.enums.CommandeStatut;
import com.couturix.domain.enums.Priorite;
import java.math.BigDecimal; import java.time.LocalDate;
public record CommandeDto(Long id, String numero, Long clientId, String clientNom, Long modeleId, String modeleNom, Long tailleurId, CommandeStatut statut, Priorite priorite, LocalDate datePrevue, LocalDate dateLivraison, int quantite, BigDecimal prixUnitaire, BigDecimal remise, BigDecimal montantTotal, String notes, boolean enRetard) {}
''')
w("commande/dto/CommandeRequest.java", '''
package com.couturix.commande.dto;
import com.couturix.domain.enums.Priorite;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal; import java.time.LocalDate;
public record CommandeRequest(@NotNull Long clientId, Long modeleId, Long compositionId, Long tailleurId, Priorite priorite, LocalDate datePrevue, int quantite, BigDecimal prixUnitaire, BigDecimal remise, String notes) {}
''')
w("commande/CommandeWorkflow.java", '''
package com.couturix.commande;
import com.couturix.common.exception.ApiException;
import com.couturix.domain.enums.CommandeStatut;
import java.util.List; import java.util.Map;
public final class CommandeWorkflow {
  private static final Map<CommandeStatut, List<CommandeStatut>> NEXT = Map.of(
    CommandeStatut.BROUILLON, List.of(CommandeStatut.CONFIRMEE, CommandeStatut.ANNULEE),
    CommandeStatut.CONFIRMEE, List.of(CommandeStatut.EN_PREPARATION, CommandeStatut.ANNULEE),
    CommandeStatut.EN_PREPARATION, List.of(CommandeStatut.EN_COUPE),
    CommandeStatut.EN_COUPE, List.of(CommandeStatut.EN_COUTURE),
    CommandeStatut.EN_COUTURE, List.of(CommandeStatut.FINITION),
    CommandeStatut.FINITION, List.of(CommandeStatut.CONTROLE),
    CommandeStatut.CONTROLE, List.of(CommandeStatut.PRETE),
    CommandeStatut.PRETE, List.of(CommandeStatut.LIVREE),
    CommandeStatut.LIVREE, List.of(),
    CommandeStatut.ANNULEE, List.of()
  );
  private CommandeWorkflow() {}
  public static void assertTransition(CommandeStatut from, CommandeStatut to) {
    if (!NEXT.getOrDefault(from, List.of()).contains(to)) {
      throw ApiException.badRequest("Transition interdite : " + from + " → " + to);
    }
  }
}
''')
print("commande entities ok")
