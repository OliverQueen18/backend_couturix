package com.couturix.production;
import com.couturix.user.User;
import jakarta.persistence.*;
import java.time.Instant;
import lombok.Getter; import lombok.Setter;
@Getter @Setter @Entity @Table(name="production_etapes")
public class ProductionEtape {
  @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
  @ManyToOne(optional=false) @JoinColumn(name="production_id") private Production production;
  @Column(nullable=false, length=30) private String etape;
  private String commentaire;
  @ManyToOne @JoinColumn(name="user_id") private User user;
  @Column(name="created_at", nullable=false) private Instant createdAt = Instant.now();
}
