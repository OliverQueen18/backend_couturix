package com.couturix.caisse;
import com.couturix.domain.enums.*;
import com.couturix.user.User;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import java.math.BigDecimal; import java.time.Instant;
import lombok.Getter; import lombok.Setter;
@Getter @Setter @Entity @Table(name="caisse_operations")
public class CaisseOperation {
  @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
  @JsonIgnore
  @ManyToOne(optional=false) @JoinColumn(name="caisse_id") private Caisse caisse;
  @Enumerated(EnumType.STRING) @Column(name="type_operation", nullable=false, length=20) private TypeOperationCaisse typeOperation;
  @Enumerated(EnumType.STRING) @Column(nullable=false, length=40) private CategorieOperationCaisse categorie;
  @Column(nullable=false, precision=14, scale=2) private BigDecimal montant;
  @Enumerated(EnumType.STRING) @Column(name="moyen_paiement", nullable=false, length=30) private MoyenPaiement moyenPaiement;
  @Column(nullable=false) private String motif;
  @Column(name="reference_metier", length=80) private String referenceMetier;
  @JsonIgnore
  @ManyToOne @JoinColumn(name="user_id") private User user;
  @Column(name="created_at", nullable=false) private Instant createdAt = Instant.now();
}
