package com.couturix.notification;

import com.couturix.domain.enums.CanalNotification;
import com.couturix.notification.dto.RappelRequest;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('SUPER_ADMIN','ADMIN','GERANT')")
public class NotificationController {
    private final NotificationService service;

    @GetMapping("/api/v1/notifications")
    public List<Notification> list() {
        return service.list();
    }

    @GetMapping("/api/v1/notification-templates")
    public List<NotificationTemplate> templates() {
        return service.templates();
    }

    @PutMapping("/api/v1/notification-templates/{id}")
    public NotificationTemplate save(@PathVariable Long id, @RequestBody NotificationTemplate t) {
        t.setId(id);
        return service.saveTemplate(t);
    }

    @PostMapping("/api/v1/notifications/rappel")
    public void rappel(@Valid @RequestBody RappelRequest body) {
        CanalNotification canal = body.canal() == null ? CanalNotification.EMAIL : body.canal();
        service.rappel(canal, body.destinataire(), body.message());
    }
}
