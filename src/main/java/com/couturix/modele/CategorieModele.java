package com.couturix.modele;
import jakarta.persistence.*;
import lombok.Getter; import lombok.Setter;
@Getter @Setter @Entity @Table(name="categories_modeles")
public class CategorieModele {
  @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
  @Column(nullable=false, unique=true, length=120) private String nom;
  @Column(nullable=false, unique=true, length=140) private String slug;
  @Column(columnDefinition="TEXT") private String description;
  @Column(name="image_url", length=500) private String imageUrl;
  @Column(nullable=false) private boolean actif = true;
}
