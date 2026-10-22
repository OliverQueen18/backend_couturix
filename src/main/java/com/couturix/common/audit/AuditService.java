package com.couturix.common.audit;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuditService {

    private final HistoriqueActionRepository repository;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void log(Long userId, String action, String entite, Long entiteId, String details) {
        HistoriqueAction event = new HistoriqueAction();
        event.setUserId(userId);
        event.setAction(action);
        event.setEntite(entite);
        event.setEntiteId(entiteId);
        event.setDetails(details);
        repository.save(event);
    }
}
