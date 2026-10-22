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
