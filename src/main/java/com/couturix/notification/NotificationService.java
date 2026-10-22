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

  public void notifyEvent(EvenementNotification event, NotificationContext ctx) {
    Map<String,String> vars = Map.of(
      "numero", ctx.numero()==null?"":ctx.numero(),
      "client", ctx.client()==null?"":ctx.client(),
      "statut", ctx.statut()==null?"":ctx.statut(),
      "montant", ctx.montant()==null?"0":ctx.montant(),
      "message", ctx.message()==null?"Mise à jour CouturiX":ctx.message()
    );
    send(event, CanalNotification.EMAIL, ctx.email(), vars);
    send(event, CanalNotification.WHATSAPP, ctx.telephone(), vars);
  }

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
