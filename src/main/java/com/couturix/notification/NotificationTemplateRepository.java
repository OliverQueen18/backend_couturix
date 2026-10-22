package com.couturix.notification;
import com.couturix.domain.enums.*; import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
public interface NotificationTemplateRepository extends JpaRepository<NotificationTemplate, Long> {
  Optional<NotificationTemplate> findByEvenementAndCanal(EvenementNotification e, CanalNotification c);
}
