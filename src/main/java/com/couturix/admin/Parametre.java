package com.couturix.admin;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "parametres")
public class Parametre {

    @Id
    @Column(name = "cle", length = 100)
    private String cle;

    @Column(nullable = false, length = 500)
    private String valeur;

    @Column(length = 255)
    private String description;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt = Instant.now();
}
