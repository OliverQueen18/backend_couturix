package com.couturix.atelier;

import com.couturix.common.domain.AuditableEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;
import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.FilterDef;
import org.hibernate.annotations.ParamDef;

@Getter
@Setter
@Entity
@Table(name = "ateliers")
@FilterDef(name = "atelierFilter", parameters = @ParamDef(name = "atelierIds", type = Long.class))
public class Atelier extends AuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id")
    private Atelier parent;

    @OneToMany(mappedBy = "parent")
    @OrderBy("nom ASC")
    private List<Atelier> enfants = new ArrayList<>();

    @Column(nullable = false, length = 180)
    private String nom;

    @Column(nullable = false, unique = true, length = 180)
    private String slug;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(length = 255)
    private String adresse;

    @Column(length = 120)
    private String ville;

    @Column(nullable = false, length = 80)
    private String pays = "Mali";

    @Column(length = 40)
    private String telephone;

    @Column(length = 180)
    private String email;

    @Column(name = "site_web", length = 255)
    private String siteWeb;

    @Column(length = 255)
    private String facebook;

    @Column(length = 255)
    private String instagram;

    @Column(length = 80)
    private String whatsapp;

    @Column(length = 80)
    private String rccm;

    @Column(length = 80)
    private String nif;

    @Column(length = 40)
    private String sigle;

    private Double latitude;

    private Double longitude;

    @Column(name = "logo_url", length = 500)
    private String logoUrl;

    @Column(nullable = false, length = 10)
    private String devise = "XOF";

    @Column(nullable = false)
    private boolean actif = true;
}
