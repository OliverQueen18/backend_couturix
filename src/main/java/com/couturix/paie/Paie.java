package com.couturix.paie;
import com.couturix.domain.enums.PaieStatut;
import com.couturix.tailleur.Tailleur;
import jakarta.persistence.*;
import java.math.BigDecimal; import java.time.Instant; import java.time.LocalDate;
import lombok.Getter; import lombok.Setter;
@Getter @Setter @Entity @Table(name="paies")
public class Paie {
  @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
  @ManyToOne(optional=false) @JoinColumn(name="tailleur_id") private Tailleur tailleur;
  @Column(name="periode_debut", nullable=false) private LocalDate periodeDebut;
  @Column(name="periode_fin", nullable=false) private LocalDate periodeFin;
  @Column(name="montant_brut", nullable=false, precision=14, scale=2) private BigDecimal montantBrut = BigDecimal.ZERO;
  @Column(nullable=false, precision=14, scale=2) private BigDecimal primes = BigDecimal.ZERO;
  @Column(nullable=false, precision=14, scale=2) private BigDecimal retenues = BigDecimal.ZERO;
  @Column(nullable=false, precision=14, scale=2) private BigDecimal avances = BigDecimal.ZERO;
  @Column(name="montant_net", nullable=false, precision=14, scale=2) private BigDecimal montantNet = BigDecimal.ZERO;
  @Enumerated(EnumType.STRING) @Column(nullable=false, length=20) private PaieStatut statut = PaieStatut.BROUILLON;
  @Column(columnDefinition="TEXT") private String notes;
  @Column(name="created_at", nullable=false) private Instant createdAt = Instant.now();
  @Column(name="updated_at", nullable=false) private Instant updatedAt = Instant.now();
}
