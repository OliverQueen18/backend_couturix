package com.couturix.security;

import com.couturix.common.exception.ApiException;
import java.util.Optional;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

public final class SecurityUtils {

    private SecurityUtils() {}

    public static Optional<UserPrincipal> currentUserOptional() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !(auth.getPrincipal() instanceof UserPrincipal principal)) {
            return Optional.empty();
        }
        return Optional.of(principal);
    }

    public static UserPrincipal currentUser() {
        return currentUserOptional().orElseThrow(() -> ApiException.unauthorized("Non authentifié"));
    }

    public static Long currentUserId() {
        return currentUser().getId();
    }

    public static boolean hasRole(String role) {
        return currentUserOptional()
                .map(p -> p.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_" + role)))
                .orElse(false);
    }
}
