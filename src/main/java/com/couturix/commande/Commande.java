package com.couturix.commande;

import com.couturix.client.Client;
import com.couturix.common.domain.TenantEntity;
import com.couturix.composition.Composition;
import com.couturix.devis.Devis;
import com.couturix.domain.enums.CommandeStatut;
import com.couturix.domain.enums.Priorite;
import com.couturix.modele.Modele;
import com.couturix.tailleur.Tailleur;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "commandes")
public class Commande extends TenantEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 40)
    private String numero;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "client_id", nullable = false)
    private Client client;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "modele_id")
    private Modele modele;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "composition_id")
    private Composition composition;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "devis_id")
    private Devis devis;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tailleur_id")
    private Tailleur tailleur;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private CommandeStatut statut = CommandeStatut.BROUILLON;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Priorite priorite = Priorite.NORMALE;

    @Column(name = "date_prevue")
    private LocalDate datePrevue;

    @Column(name = "date_livraison")
    private LocalDate dateLivraison;

    @Column(nullable = false)
    private int quantite = 1;

    @Column(name = "prix_unitaire", nullable = false, precision = 14, scale = 2)
    private BigDecimal prixUnitaire = BigDecimal.ZERO;

    @Column(nullable = false, precision = 14, scale = 2)
    private BigDecimal remise = BigDecimal.ZERO;

    @Column(name = "montant_total", nullable = false, precision = 14, scale = 2)
    private BigDecimal montantTotal = BigDecimal.ZERO;

    @Column(columnDefinition = "TEXT")
    private String notes;

    @OneToMany(mappedBy = "commande", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<CommandeDetail> details = new ArrayList<>();

    @OneToOne(mappedBy = "commande", cascade = CascadeType.ALL, orphanRemoval = true)
    private CommandeMesure mesure;

    @OneToMany(mappedBy = "commande", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("createdAt ASC")
    private List<CommandeStatutHistorique> historique = new ArrayList<>();

    public boolean isRetard() {
        return datePrevue != null
                && datePrevue.isBefore(LocalDate.now())
                && statut != CommandeStatut.LIVREE
                && statut != CommandeStatut.ANNULEE;
    }

    public void recalculerMontant() {
        BigDecimal brut = prixUnitaire.multiply(BigDecimal.valueOf(Math.max(quantite, 1)));
        montantTotal = brut.subtract(remise == null ? BigDecimal.ZERO : remise).max(BigDecimal.ZERO);
    }
}
