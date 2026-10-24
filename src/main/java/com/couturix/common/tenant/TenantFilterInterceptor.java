package com.couturix.common.tenant;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.List;
import org.hibernate.Session;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class TenantFilterInterceptor implements HandlerInterceptor {

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        String path = request.getRequestURI();
        if (path.startsWith("/api/v1/public/") || path.startsWith("/api/v1/avis/")) {
            return true;
        }
        TenantContext.current().ifPresent(snapshot -> {
            Long scoped = parseAtelierId(request.getParameter("atelierId"));
            if (snapshot.bypass()) {
                if (scoped != null) {
                    entityManager.unwrap(Session.class)
                            .enableFilter("atelierFilter")
                            .setParameterList("atelierIds", List.of(scoped));
                }
                return;
            }
            if (snapshot.scopeIds() != null && !snapshot.scopeIds().isEmpty()) {
                entityManager.unwrap(Session.class)
                        .enableFilter("atelierFilter")
                        .setParameterList("atelierIds", snapshot.scopeIds());
            }
        });
        return true;
    }

    private Long parseAtelierId(String raw) {
        if (raw == null || raw.isBlank()) {
            return null;
        }
        try {
            return Long.parseLong(raw.trim());
        } catch (NumberFormatException ex) {
            return null;
        }
    }
}
