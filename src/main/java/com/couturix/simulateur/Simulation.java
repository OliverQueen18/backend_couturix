package com.couturix.simulateur;

import com.couturix.atelier.Atelier;
import com.couturix.common.tenant.TenantAssigner;
import com.couturix.common.tenant.TenantOwned;
import com.couturix.composition.Composition;
import com.couturix.modele.Modele;
import com.couturix.user.User;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.Instant;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.Filter;

@Getter
@Setter
@Entity
@Table(name = "simulations")
@Filter(name = "atelierFilter", condition = "atelier_id IN (:atelierIds)")
@EntityListeners(TenantAssigner.class)
public class Simulation implements TenantOwned {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "atelier_id")
    private Atelier atelier;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "modele_id")
    private Modele modele;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "composition_id")
    private Composition composition;

    @Column(length = 180)
    private String libelle;

    @Column(nullable = false)
    private int quantite = 1;

    @Column(name = "cout_matiere", nullable = false, precision = 14, scale = 2)
    private BigDecimal coutMatiere = BigDecimal.ZERO;

    @Column(name = "cout_main_oeuvre", nullable = false, precision = 14, scale = 2)
    private BigDecimal coutMainOeuvre = BigDecimal.ZERO;

    @Column(name = "frais_indirects", nullable = false, precision = 14, scale = 2)
    private BigDecimal fraisIndirects = BigDecimal.ZERO;

    @Column(name = "cout_total", nullable = false, precision = 14, scale = 2)
    private BigDecimal coutTotal = BigDecimal.ZERO;

    @Column(name = "prix_revient", nullable = false, precision = 14, scale = 2)
    private BigDecimal prixRevient = BigDecimal.ZERO;

    @Column(name = "prix_vente", nullable = false, precision = 14, scale = 2)
    private BigDecimal prixVente = BigDecimal.ZERO;

    @Column(nullable = false, precision = 14, scale = 2)
    private BigDecimal remise = BigDecimal.ZERO;

    @Column(nullable = false, precision = 14, scale = 2)
    private BigDecimal benefice = BigDecimal.ZERO;

    @Column(nullable = false, precision = 8, scale = 4)
    private BigDecimal marge = BigDecimal.ZERO;

    @Column(columnDefinition = "TEXT")
    private String payload;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt = Instant.now();
}
