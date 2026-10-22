package com.couturix.modele;
import com.couturix.client.Client;
import jakarta.persistence.*;
import java.time.Instant;
import lombok.Getter; import lombok.Setter;
@Getter @Setter @Entity @Table(name="favoris")
public class Favori {
  @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
  @ManyToOne(optional=false) @JoinColumn(name="client_id") private Client client;
  @ManyToOne(optional=false) @JoinColumn(name="modele_id") private Modele modele;
  @Column(name="created_at", nullable=false) private Instant createdAt = Instant.now();
}
