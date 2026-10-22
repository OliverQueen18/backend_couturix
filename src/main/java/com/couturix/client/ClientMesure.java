package com.couturix.client;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "client_mesures")
public class ClientMesure {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "client_id", nullable = false)
    private Client client;

    @Column(name = "tour_cou", precision = 8, scale = 2)
    private BigDecimal tourCou;

    @Column(name = "tour_poitrine", precision = 8, scale = 2)
    private BigDecimal tourPoitrine;

    @Column(name = "tour_taille", precision = 8, scale = 2)
    private BigDecimal tourTaille;

    @Column(name = "tour_hanches", precision = 8, scale = 2)
    private BigDecimal tourHanches;

    @Column(name = "tour_bras", precision = 8, scale = 2)
    private BigDecimal tourBras;

    @Column(name = "tour_poignet", precision = 8, scale = 2)
    private BigDecimal tourPoignet;

    @Column(precision = 8, scale = 2)
    private BigDecimal epaule;

    @Column(name = "longueur_dos", precision = 8, scale = 2)
    private BigDecimal longueurDos;

    @Column(name = "longueur_devant", precision = 8, scale = 2)
    private BigDecimal longueurDevant;

    @Column(name = "longueur_manche", precision = 8, scale = 2)
    private BigDecimal longueurManche;

    @Column(name = "tour_cuisse", precision = 8, scale = 2)
    private BigDecimal tourCuisse;

    @Column(name = "tour_genou", precision = 8, scale = 2)
    private BigDecimal tourGenou;

    @Column(name = "bas_pantalon", precision = 8, scale = 2)
    private BigDecimal basPantalon;

    @Column(name = "longueur_pantalon", precision = 8, scale = 2)
    private BigDecimal longueurPantalon;

    @Column(precision = 8, scale = 2)
    private BigDecimal hauteur;

    @Column(columnDefinition = "TEXT")
    private String notes;

    @Column(name = "date_prise", nullable = false)
    private LocalDate datePrise = LocalDate.now();

    @Column(nullable = false)
    private boolean courante = true;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt = Instant.now();
}
