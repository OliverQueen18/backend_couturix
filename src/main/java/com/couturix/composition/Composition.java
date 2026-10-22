package com.couturix.composition;
import com.couturix.modele.Modele;
import jakarta.persistence.*;
import java.time.Instant; import java.util.*;
import lombok.Getter; import lombok.Setter;
@Getter @Setter @Entity @Table(name="compositions")
public class Composition {
  @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
  @ManyToOne(optional=false) @JoinColumn(name="modele_id") private Modele modele;
  @Column(nullable=false) private int version;
  private String libelle;
  @Column(nullable=false) private boolean active = true;
  @Column(name="created_at", nullable=false) private Instant createdAt = Instant.now();
  @OneToMany(mappedBy="composition", cascade=CascadeType.ALL, orphanRemoval=true) private List<CompositionLigne> lignes = new ArrayList<>();
}
