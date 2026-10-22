package com.couturix.paiement;
import com.couturix.atelier.Atelier;
import com.couturix.client.Client;
import com.couturix.commande.Commande;
import com.couturix.common.tenant.TenantAssigner;
import com.couturix.common.tenant.TenantOwned;
import com.couturix.domain.enums.MoyenPaiement;
import com.couturix.user.User;
import jakarta.persistence.*;
import java.math.BigDecimal; import java.time.Instant; import java.time.LocalDate;
import lombok.Getter; import lombok.Setter;
import org.hibernate.annotations.Filter;
@Getter @Setter @Entity @Table(name="paiements")
@Filter(name = "atelierFilter", condition = "atelier_id IN (:atelierIds)")
@EntityListeners(TenantAssigner.class)
public class Paiement implements TenantOwned {
  @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
  @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "atelier_id") private Atelier atelier;
  @ManyToOne @JoinColumn(name="commande_id") private Commande commande;
  @ManyToOne(optional=false) @JoinColumn(name="client_id") private Client client;
  @Column(nullable=false, precision=14, scale=2) private BigDecimal montant;
  @Enumerated(EnumType.STRING) @Column(nullable=false, length=30) private MoyenPaiement moyen;
  @Column(name="date_paiement", nullable=false) private LocalDate datePaiement = LocalDate.now();
  private String reference; private String notes;
  @ManyToOne @JoinColumn(name="user_id") private User user;
  @Column(name="created_at", nullable=false) private Instant createdAt = Instant.now();
}
