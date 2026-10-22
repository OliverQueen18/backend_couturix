package com.couturix.modele;
import jakarta.persistence.*;
import lombok.Getter; import lombok.Setter;
@Getter @Setter @Entity @Table(name="modele_images")
public class ModeleImage {
  @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
  @ManyToOne(optional=false) @JoinColumn(name="modele_id") private Modele modele;
  @Column(nullable=false, length=500) private String url;
  @Column(nullable=false) private int ordre;
  @Column(nullable=false) private boolean principale;
}
