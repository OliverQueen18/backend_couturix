package com.couturix.common.tenant;

import com.couturix.atelier.Atelier;
import java.util.List;
import java.util.Optional;

public final class TenantContext {

    private static final ThreadLocal<Snapshot> CURRENT = new ThreadLocal<>();

    private TenantContext() {}

    public record Snapshot(Long atelierId, String atelierNom, List<Long> scopeIds, boolean bypass, Atelier atelier) {}

    public static void set(Snapshot snapshot) {
        CURRENT.set(snapshot);
    }

    public static void clear() {
        CURRENT.remove();
    }

    public static Optional<Snapshot> current() {
        return Optional.ofNullable(CURRENT.get());
    }

    public static Optional<Atelier> atelier() {
        return current().map(Snapshot::atelier);
    }
}
