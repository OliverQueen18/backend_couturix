package com.couturix.abonnement;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Getter
@Setter
@Entity
@Table(name = "plan_offres")
public class PlanOffre {

    @Id
    @Column(length = 20)
    private String code;

    @Column(nullable = false, length = 80)
    private String libelle;

    @Column(name = "trial_days", nullable = false)
    private int trialDays;

    @Column(name = "max_ateliers")
    private Integer maxAteliers;

    @Column(nullable = false)
    private boolean sms;

    @Column(name = "prix_mensuel", nullable = false)
    private int prixMensuel;

    @Column(name = "remise_annuelle", nullable = false)
    private int remiseAnnuelle = 20;

    @Column(length = 80)
    private String kicker;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(length = 80)
    private String cta;

    @Column(nullable = false)
    private boolean popular;

    @Column(nullable = false)
    private boolean actif = true;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb", nullable = false)
    private List<String> features = new ArrayList<>();

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb", nullable = false)
    private List<PlanServiceGroup> services = new ArrayList<>();

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt = Instant.now();

    @PreUpdate
    void touch() {
        this.updatedAt = Instant.now();
    }
}
