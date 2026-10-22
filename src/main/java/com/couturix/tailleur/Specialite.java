package com.couturix.tailleur;
import jakarta.persistence.*;
import lombok.Getter; import lombok.Setter;
@Getter @Setter @Entity @Table(name="specialites")
public class Specialite {
  @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
  @Column(nullable=false, unique=true, length=120) private String libelle;
}
