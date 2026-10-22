package com.couturix.common.tenant;

import jakarta.persistence.PrePersist;

public class TenantAssigner {

    @PrePersist
    public void assign(Object entity) {
        if (entity instanceof TenantOwned owned && owned.getAtelier() == null) {
            TenantContext.atelier().ifPresent(owned::setAtelier);
        }
    }
}
