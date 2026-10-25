package com.couturix.site;

import com.couturix.common.domain.TimestampedEntity;
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
@Table(name = "site_media")
public class SiteMedia extends TimestampedEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "original_name", nullable = false, length = 180)
    private String originalName;

    @Column(nullable = false, length = 500)
    private String url;

    @Column(length = 80)
    private String mime;

    @Column(name = "size_bytes", nullable = false)
    private long sizeBytes;
}
