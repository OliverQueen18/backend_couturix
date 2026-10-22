package com.couturix.common.tenant;

import com.couturix.atelier.Atelier;

public interface TenantOwned {
    Atelier getAtelier();
    void setAtelier(Atelier atelier);
}
