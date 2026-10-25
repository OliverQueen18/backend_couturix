package com.couturix.site;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "site_slides")
public class SiteSlide {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(length = 80)
    private String kicker;

    @Column(nullable = false, length = 180)
    private String title;

    @Column(length = 400)
    private String body;

    @Column(name = "image_url", nullable = false, length = 500)
    private String imageUrl;

    @Column(name = "cta_label", length = 80)
    private String ctaLabel;

    @Column(name = "cta_path", length = 180)
    private String ctaPath;

    @Column(name = "sort_order", nullable = false)
    private int sortOrder = 0;

    @Column(nullable = false)
    private boolean visible = true;

    @Column(name = "show_text", nullable = false)
    private boolean showText = true;
}
