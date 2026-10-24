package com.couturix.abonnement;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import java.time.Instant;
import lombok.Getter;
import lombok.Setter;

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

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt = Instant.now();

    @PreUpdate
    void touch() {
        this.updatedAt = Instant.now();
    }
}
