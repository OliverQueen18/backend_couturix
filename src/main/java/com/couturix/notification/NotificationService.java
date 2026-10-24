package com.couturix.notification;

import com.couturix.abonnement.AbonnementService;
import com.couturix.commande.Commande;
import com.couturix.config.CouturixProperties;
import com.couturix.domain.enums.CanalNotification;
import com.couturix.domain.enums.EvenementNotification;
import com.couturix.domain.enums.NotificationStatut;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationService {
    private final NotificationRepository notificationRepository;
    private final NotificationTemplateRepository templateRepository;
    private final JavaMailSender mailSender;
    private final CouturixProperties properties;
    private final SmsGatewayService smsGatewayService;
    private final AbonnementService abonnementService;

    public void notifyEvent(EvenementNotification event, NotificationContext ctx) {
        Map<String, String> vars = vars(ctx.numero(), ctx.client(), ctx.statut(), ctx.montant(), ctx.message());
        send(event, CanalNotification.EMAIL, ctx.email(), vars);
        send(event, CanalNotification.WHATSAPP, ctx.telephone(), vars);
        if (abonnementService.smsAutorise()) {
            send(event, CanalNotification.SMS, ctx.telephone(), vars);
        }
    }

    public void notifyEvent(EvenementNotification event, Commande commande) {
        Map<String, String> vars = vars(
                commande.getNumero(),
                commande.getClient().nomComplet(),
                commande.getStatut().name(),
                commande.getMontantTotal() == null ? "0" : commande.getMontantTotal().toPlainString(),
                "Mise à jour CouturiX");
        send(event, CanalNotification.EMAIL, commande.getClient().getEmail(), vars);
        send(event, CanalNotification.WHATSAPP, commande.getClient().getTelephone(), vars);
        if (abonnementService.smsAutorise()) {
            send(event, CanalNotification.SMS, commande.getClient().getTelephone(), vars);
        }
    }

    public void rappel(CanalNotification canal, String destinataire, String message) {
        if (canal == CanalNotification.SMS) {
            abonnementService.assertSms();
        }
        send(EvenementNotification.RAPPEL, canal, destinataire, vars("", "", "", "", message));
    }

    public List<Notification> list() {
        return notificationRepository.findAll();
    }

    public List<NotificationTemplate> templates() {
        return templateRepository.findAll();
    }

    public NotificationTemplate saveTemplate(NotificationTemplate t) {
        return templateRepository.save(t);
    }

    @Async
    public void send(EvenementNotification event, CanalNotification canal, String destinataire, Map<String, String> vars) {
        if (destinataire == null || destinataire.isBlank()) {
            return;
        }
        NotificationTemplate tpl = templateRepository.findByEvenementAndCanal(event, canal).orElse(null);
        String sujet = tpl == null ? event.name() : apply(tpl.getSujet(), vars);
        String corps = tpl == null ? event.name() : apply(tpl.getCorps(), vars);
        Notification n = new Notification();
        n.setCanal(canal);
        n.setEvenement(event);
        n.setDestinataire(destinataire);
        n.setSujet(sujet);
        n.setContenu(corps);
        try {
            if (canal == CanalNotification.EMAIL) {
                SimpleMailMessage msg = new SimpleMailMessage();
                msg.setFrom(properties.notifications().fromEmail());
                msg.setTo(destinataire);
                msg.setSubject(sujet == null ? "CouturiX" : sujet);
                msg.setText(corps);
                mailSender.send(msg);
            } else if (canal == CanalNotification.SMS) {
                if (!smsGatewayService.isReady()) {
                    log.info("SMS Gateway désactivé, message simulé vers {}: {}", destinataire, corps);
                } else if (!smsGatewayService.sendSms(destinataire, corps)) {
                    throw new IllegalStateException("SMS Gateway a refusé l’envoi");
                }
            } else if (properties.notifications().whatsapp().enabled()) {
                RestClient.create().post()
                        .uri(properties.notifications().whatsapp().apiUrl()
                                + "/" + properties.notifications().whatsapp().phoneNumberId()
                                + "/messages")
                        .header("Authorization", "Bearer " + properties.notifications().whatsapp().accessToken())
                        .body(Map.of(
                                "messaging_product", "whatsapp",
                                "to", destinataire,
                                "type", "text",
                                "text", Map.of("body", corps)))
                        .retrieve()
                        .toBodilessEntity();
            } else {
                log.info("WhatsApp désactivé, message simulé vers {}: {}", destinataire, corps);
            }
            n.setStatut(NotificationStatut.ENVOYEE);
            n.setSentAt(Instant.now());
        } catch (Exception ex) {
            log.warn("Notification {} échouée: {}", canal, ex.getMessage());
            n.setStatut(NotificationStatut.ECHEC);
            n.setErreur(ex.getMessage());
        }
        notificationRepository.save(n);
    }

    private Map<String, String> vars(String numero, String client, String statut, String montant, String message) {
        return Map.of(
                "numero", numero == null ? "" : numero,
                "client", client == null ? "" : client,
                "statut", statut == null ? "" : statut,
                "montant", montant == null ? "0" : montant,
                "message", message == null || message.isBlank() ? "Mise à jour CouturiX" : message);
    }

    private String apply(String template, Map<String, String> vars) {
        if (template == null) {
            return null;
        }
        String out = template;
        for (var e : vars.entrySet()) {
            out = out.replace("{{" + e.getKey() + "}}", e.getValue());
        }
        return out;
    }
}
