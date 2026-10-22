package com.couturix.notification;
import com.couturix.domain.enums.*;
import jakarta.persistence.*;
import java.time.Instant;
import lombok.Getter; import lombok.Setter;
@Getter @Setter @Entity @Table(name="notifications")
public class Notification {
  @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
  @Enumerated(EnumType.STRING) @Column(nullable=false, length=20) private CanalNotification canal;
  @Enumerated(EnumType.STRING) @Column(nullable=false, length=40) private EvenementNotification evenement;
  @Column(nullable=false, length=180) private String destinataire;
  private String sujet;
  @Column(nullable=false, columnDefinition="TEXT") private String contenu;
  @Enumerated(EnumType.STRING) @Column(nullable=false, length=20) private NotificationStatut statut = NotificationStatut.EN_ATTENTE;
  @Column(columnDefinition="TEXT") private String erreur;
  @Column(name="created_at", nullable=false) private Instant createdAt = Instant.now();
  @Column(name="sent_at") private Instant sentAt;
}
