# -*- coding: utf-8 -*-
from pathlib import Path
ROOT = Path(__file__).resolve().parents[1] / "src" / "main" / "java" / "com" / "couturix"

def w(rel, content):
    p = ROOT / rel
    p.parent.mkdir(parents=True, exist_ok=True)
    p.write_text(content.strip() + "\n", encoding="utf-8")

w("production/Production.java", '''
package com.couturix.production;
import com.couturix.commande.Commande;
import com.couturix.tailleur.Tailleur;
import jakarta.persistence.*;
import java.time.Instant;
import lombok.Getter; import lombok.Setter;
@Getter @Setter @Entity @Table(name="production")
public class Production {
  @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
  @OneToOne(optional=false) @JoinColumn(name="commande_id") private Commande commande;
  @ManyToOne @JoinColumn(name="tailleur_id") private Tailleur tailleur;
  @Column(name="date_debut") private Instant dateDebut;
  @Column(name="date_fin") private Instant dateFin;
  @Column(nullable=false, length=30) private String statut = "EN_ATTENTE";
  @Column(name="created_at", nullable=false) private Instant createdAt = Instant.now();
}
''')
w("production/ProductionEtape.java", '''
package com.couturix.production;
import com.couturix.user.User;
import jakarta.persistence.*;
import java.time.Instant;
import lombok.Getter; import lombok.Setter;
@Getter @Setter @Entity @Table(name="production_etapes")
public class ProductionEtape {
  @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
  @ManyToOne(optional=false) @JoinColumn(name="production_id") private Production production;
  @Column(nullable=false, length=30) private String etape;
  private String commentaire;
  @ManyToOne @JoinColumn(name="user_id") private User user;
  @Column(name="created_at", nullable=false) private Instant createdAt = Instant.now();
}
''')
w("production/ProductionRepository.java", "package com.couturix.production;\nimport com.couturix.commande.Commande;\nimport java.util.Optional; import java.util.List;\nimport org.springframework.data.jpa.repository.JpaRepository;\npublic interface ProductionRepository extends JpaRepository<Production, Long> {\n  Optional<Production> findByCommande(Commande commande);\n  List<Production> findByTailleurId(Long tailleurId);\n}")
w("production/ProductionEtapeRepository.java", "package com.couturix.production;\nimport org.springframework.data.jpa.repository.JpaRepository;\npublic interface ProductionEtapeRepository extends JpaRepository<ProductionEtape, Long> {}")
w("production/ProductionService.java", '''
package com.couturix.production;
import com.couturix.commande.Commande;
import com.couturix.common.exception.ApiException;
import java.time.Instant; import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service; import org.springframework.transaction.annotation.Transactional;
@Service @RequiredArgsConstructor
public class ProductionService {
  private final ProductionRepository productionRepository;
  private final ProductionEtapeRepository etapeRepository;

  @Transactional
  public Production ensureFor(Commande commande) {
    return productionRepository.findByCommande(commande).orElseGet(() -> {
      Production p = new Production(); p.setCommande(commande); p.setTailleur(commande.getTailleur());
      p.setDateDebut(Instant.now()); p.setStatut("EN_COURS");
      return productionRepository.save(p);
    });
  }

  @Transactional
  public void recordEtape(Commande commande, String etape, String commentaire) {
    Production p = ensureFor(commande);
    p.setTailleur(commande.getTailleur());
    ProductionEtape e = new ProductionEtape(); e.setProduction(p); e.setEtape(etape); e.setCommentaire(commentaire);
    etapeRepository.save(e);
  }

  public List<Production> list() { return productionRepository.findAll(); }
  public Production get(Long id) { return productionRepository.findById(id).orElseThrow(() -> ApiException.notFound("Production introuvable")); }
}
''')
w("production/ProductionController.java", '''
package com.couturix.production;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
@RestController @RequestMapping("/api/v1/productions") @RequiredArgsConstructor
@PreAuthorize("hasAnyRole('SUPER_ADMIN','ADMIN','GERANT','TAILLEUR')")
public class ProductionController {
  private final ProductionService service;
  @GetMapping public List<Production> list() { return service.list(); }
  @GetMapping("/{id}") public Production get(@PathVariable Long id) { return service.get(id); }
}
''')

w("notification/Notification.java", '''
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
''')
w("notification/NotificationTemplate.java", '''
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
''')
w("notification/NotificationRepository.java", "package com.couturix.notification;\nimport org.springframework.data.jpa.repository.JpaRepository;\npublic interface NotificationRepository extends JpaRepository<Notification, Long> {}")
w("notification/NotificationTemplateRepository.java", "package com.couturix.notification;\nimport com.couturix.domain.enums.*; import java.util.Optional;\nimport org.springframework.data.jpa.repository.JpaRepository;\npublic interface NotificationTemplateRepository extends JpaRepository<NotificationTemplate, Long> {\n  Optional<NotificationTemplate> findByEvenementAndCanal(EvenementNotification e, CanalNotification c);\n}")
w("notification/NotificationService.java", '''
package com.couturix.notification;
import com.couturix.commande.Commande;
import com.couturix.config.CouturixProperties;
import com.couturix.domain.enums.*;
import java.time.Instant; import java.util.List; import java.util.Map;
import lombok.RequiredArgsConstructor; import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.SimpleMailMessage; import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service; import org.springframework.web.client.RestClient;

@Slf4j @Service @RequiredArgsConstructor
public class NotificationService {
  private final NotificationRepository notificationRepository;
  private final NotificationTemplateRepository templateRepository;
  private final JavaMailSender mailSender;
  private final CouturixProperties properties;

  public void notifyEvent(EvenementNotification event, Commande commande) {
    Map<String,String> vars = Map.of(
      "numero", commande.getNumero(),
      "client", commande.getClient().nomComplet(),
      "statut", commande.getStatut().name(),
      "montant", commande.getMontantTotal()==null?"0":commande.getMontantTotal().toPlainString(),
      "message", "Mise à jour CouturiX"
    );
    String destEmail = commande.getClient().getEmail();
    String destTel = commande.getClient().getTelephone();
    send(event, CanalNotification.EMAIL, destEmail, vars);
    send(event, CanalNotification.WHATSAPP, destTel, vars);
  }

  public List<Notification> list() { return notificationRepository.findAll(); }
  public List<NotificationTemplate> templates() { return templateRepository.findAll(); }
  public NotificationTemplate saveTemplate(NotificationTemplate t) { return templateRepository.save(t); }

  @Async
  public void send(EvenementNotification event, CanalNotification canal, String destinataire, Map<String,String> vars) {
    if (destinataire == null || destinataire.isBlank()) return;
    NotificationTemplate tpl = templateRepository.findByEvenementAndCanal(event, canal).orElse(null);
    String sujet = tpl==null ? event.name() : apply(tpl.getSujet(), vars);
    String corps = tpl==null ? event.name() : apply(tpl.getCorps(), vars);
    Notification n = new Notification(); n.setCanal(canal); n.setEvenement(event); n.setDestinataire(destinataire); n.setSujet(sujet); n.setContenu(corps);
    try {
      if (canal == CanalNotification.EMAIL) {
        SimpleMailMessage msg = new SimpleMailMessage();
        msg.setFrom(properties.notifications().fromEmail()); msg.setTo(destinataire); msg.setSubject(sujet==null?"CouturiX":sujet); msg.setText(corps);
        mailSender.send(msg);
      } else if (properties.notifications().whatsapp().enabled()) {
        RestClient.create().post().uri(properties.notifications().whatsapp().apiUrl() + "/" + properties.notifications().whatsapp().phoneNumberId() + "/messages")
          .header("Authorization", "Bearer " + properties.notifications().whatsapp().accessToken())
          .body(Map.of("messaging_product","whatsapp","to", destinataire, "type","text", "text", Map.of("body", corps)))
          .retrieve().toBodilessEntity();
      } else {
        log.info("WhatsApp désactivé, message simulé vers {}: {}", destinataire, corps);
      }
      n.setStatut(NotificationStatut.ENVOYEE); n.setSentAt(Instant.now());
    } catch (Exception ex) {
      log.warn("Notification {} échouée: {}", canal, ex.getMessage());
      n.setStatut(NotificationStatut.ECHEC); n.setErreur(ex.getMessage());
    }
    notificationRepository.save(n);
  }

  private String apply(String template, Map<String,String> vars) {
    if (template==null) return null;
    String out = template;
    for (var e : vars.entrySet()) out = out.replace("{{"+e.getKey()+"}}", e.getValue());
    return out;
  }
}
''')
w("notification/NotificationController.java", '''
package com.couturix.notification;
import java.util.List; import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import com.couturix.domain.enums.*;
@RestController @RequiredArgsConstructor
@PreAuthorize("hasAnyRole('SUPER_ADMIN','ADMIN','GERANT')")
public class NotificationController {
  private final NotificationService service;
  @GetMapping("/api/v1/notifications") public List<Notification> list() { return service.list(); }
  @GetMapping("/api/v1/notification-templates") public List<NotificationTemplate> templates() { return service.templates(); }
  @PutMapping("/api/v1/notification-templates/{id}") public NotificationTemplate save(@PathVariable Long id, @RequestBody NotificationTemplate t) { t.setId(id); return service.saveTemplate(t); }
  @PostMapping("/api/v1/notifications/rappel") public void rappel(@RequestBody Map<String,String> body) {
    service.send(EvenementNotification.RAPPEL, CanalNotification.EMAIL, body.get("destinataire"), Map.of("message", body.getOrDefault("message","Rappel CouturiX"), "numero","", "client","", "statut","", "montant",""));
  }
}
''')
print("prod+notif ok")
