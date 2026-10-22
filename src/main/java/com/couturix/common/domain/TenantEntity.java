package com.couturix.common.domain;

import com.couturix.atelier.Atelier;
import com.couturix.common.tenant.TenantAssigner;
import com.couturix.common.tenant.TenantOwned;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MappedSuperclass;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.Filter;

@Getter
@Setter
@MappedSuperclass
@Filter(name = "atelierFilter", condition = "atelier_id IN (:atelierIds)")
@EntityListeners(TenantAssigner.class)
public abstract class TenantEntity extends AuditableEntity implements TenantOwned {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "atelier_id")
    private Atelier atelier;
}
