package com.couturix.paiement;
import com.couturix.atelier.Atelier;
import com.couturix.client.Client;
import com.couturix.commande.Commande;
import com.couturix.common.tenant.TenantAssigner;
import com.couturix.common.tenant.TenantOwned;
import com.couturix.domain.enums.MoyenPaiement;
import com.couturix.user.User;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import java.math.BigDecimal; import java.time.Instant; import java.time.LocalDate;
import lombok.Getter; import lombok.Setter;
import org.hibernate.annotations.Filter;
@Getter @Setter @Entity @Table(name="paiements")
@Filter(name = "atelierFilter", condition = "atelier_id IN (:atelierIds)")
@EntityListeners(TenantAssigner.class)
public class Paiement implements TenantOwned {
  @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
  @JsonIgnore
  @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "atelier_id") private Atelier atelier;
  @JsonIgnore
  @ManyToOne @JoinColumn(name="commande_id") private Commande commande;
  @JsonIgnore
  @ManyToOne(optional=false) @JoinColumn(name="client_id") private Client client;
  @Column(nullable=false, precision=14, scale=2) private BigDecimal montant;
  @Enumerated(EnumType.STRING) @Column(nullable=false, length=30) private MoyenPaiement moyen;
  @Column(name="date_paiement", nullable=false) private LocalDate datePaiement = LocalDate.now();
  private String reference; private String notes;
  @JsonIgnore
  @ManyToOne @JoinColumn(name="user_id") private User user;
  @Column(name="created_at", nullable=false) private Instant createdAt = Instant.now();

  @JsonProperty("clientId")
  public Long jsonClientId() {
    return client == null ? null : client.getId();
  }

  @JsonProperty("clientNom")
  public String jsonClientNom() {
    return client == null ? null : client.nomComplet();
  }

  @JsonProperty("commandeId")
  public Long jsonCommandeId() {
    return commande == null ? null : commande.getId();
  }
}
