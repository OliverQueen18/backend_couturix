package com.couturix.caisse;
import jakarta.persistence.*;
import java.math.BigDecimal; import java.time.Instant;
import lombok.Getter; import lombok.Setter;
@Getter @Setter @Entity @Table(name="clotures_caisse")
public class ClotureCaisse {
  @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
  @OneToOne(optional=false) @JoinColumn(name="caisse_id") private Caisse caisse;
  @Column(nullable=false, precision=14, scale=2) private BigDecimal ecart = BigDecimal.ZERO;
  @Column(columnDefinition="TEXT") private String commentaires;
  @Column(name="created_at", nullable=false) private Instant createdAt = Instant.now();
}
