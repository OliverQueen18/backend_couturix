package com.couturix.notification;
import com.couturix.domain.enums.*;
import jakarta.persistence.*;
import lombok.Getter; import lombok.Setter;
@Getter @Setter @Entity @Table(name="notification_templates")
public class NotificationTemplate {
  @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
  @Enumerated(EnumType.STRING) @Column(nullable=false, length=40) private EvenementNotification evenement;
  @Enumerated(EnumType.STRING) @Column(nullable=false, length=20) private CanalNotification canal;
  private String sujet;
  @Column(nullable=false, columnDefinition="TEXT") private String corps;
}
