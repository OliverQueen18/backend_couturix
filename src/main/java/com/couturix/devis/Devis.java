package com.couturix.devis;

import com.couturix.atelier.Atelier;
import com.couturix.client.Client;
import com.couturix.common.domain.TimestampedEntity;
import com.couturix.common.tenant.TenantAssigner;
import com.couturix.common.tenant.TenantOwned;
import com.couturix.composition.Composition;
import com.couturix.domain.enums.DevisStatut;
import com.couturix.modele.Modele;
import com.couturix.simulateur.Simulation;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.LocalDate;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.Filter;

@Getter
@Setter
@Entity
@Table(name = "devis")
@Filter(name = "atelierFilter", condition = "atelier_id IN (:atelierIds)")
@EntityListeners(TenantAssigner.class)
public class Devis extends TimestampedEntity implements TenantOwned {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "atelier_id")
    private Atelier atelier;

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
    @JoinColumn(name = "simulation_id")
    private Simulation simulation;

    @Column(nullable = false)
    private int quantite = 1;

    @Column(name = "prix_unitaire", nullable = false, precision = 14, scale = 2)
    private BigDecimal prixUnitaire = BigDecimal.ZERO;

    @Column(nullable = false, precision = 14, scale = 2)
    private BigDecimal remise = BigDecimal.ZERO;

    @Column(name = "montant_total", nullable = false, precision = 14, scale = 2)
    private BigDecimal montantTotal = BigDecimal.ZERO;

    @Column(name = "delai_jours")
    private Integer delaiJours;

    @Column(columnDefinition = "TEXT")
    private String conditions;

    private LocalDate validite;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private DevisStatut statut = DevisStatut.BROUILLON;
}
