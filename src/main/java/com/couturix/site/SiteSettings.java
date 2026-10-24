package com.couturix.site;

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
@Table(name = "site_settings")
public class SiteSettings {

    @Id
    private Short id = 1;

    @Column(name = "brand_name", nullable = false, length = 80)
    private String brandName = "CouturiX";

    @Column(length = 255)
    private String tagline;

    @Column(name = "footer_tagline", length = 255)
    private String footerTagline;

    @Column(name = "footer_note", length = 255)
    private String footerNote;

    @Column(name = "header_cta_label", length = 80)
    private String headerCtaLabel;

    @Column(name = "header_cta_path", length = 180)
    private String headerCtaPath;

    @Column(name = "contact_email", length = 180)
    private String contactEmail;

    @Column(name = "contact_phones", length = 180)
    private String contactPhones;

    @Column(name = "contact_address", length = 255)
    private String contactAddress;

    @Column(name = "facebook_url", length = 255)
    private String facebookUrl;

    @Column(name = "instagram_url", length = 255)
    private String instagramUrl;

    @Column(name = "tiktok_url", length = 255)
    private String tiktokUrl;

    @Column(name = "linkedin_url", length = 255)
    private String linkedinUrl;

    @Column(name = "youtube_url", length = 255)
    private String youtubeUrl;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt = Instant.now();

    @PreUpdate
    void touch() {
        this.updatedAt = Instant.now();
    }
}
