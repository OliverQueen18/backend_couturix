package com.couturix.paie;
import com.couturix.tailleur.Tailleur;
import jakarta.persistence.*;
import java.math.BigDecimal; import java.time.Instant; import java.time.LocalDate;
import lombok.Getter; import lombok.Setter;
@Getter @Setter @Entity @Table(name="avances")
public class Avance {
  @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
  @ManyToOne(optional=false) @JoinColumn(name="tailleur_id") private Tailleur tailleur;
  @Column(nullable=false, precision=14, scale=2) private BigDecimal montant;
  @Column(name="date_avance", nullable=false) private LocalDate dateAvance = LocalDate.now();
  private String motif;
  @Column(nullable=false, length=20) private String statut = "EN_COURS";
  @Column(name="created_at", nullable=false) private Instant createdAt = Instant.now();
}
