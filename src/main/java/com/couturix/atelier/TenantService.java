package com.couturix.atelier;

import com.couturix.common.tenant.TenantContext;
import com.couturix.user.User;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class TenantService {

    private final AtelierRepository atelierRepository;

    @Transactional(readOnly = true)
    public TenantContext.Snapshot snapshotFor(User user) {
        boolean superAdmin = user.roleCodes().stream().anyMatch(code -> "SUPER_ADMIN".equals(code.name()));
        Atelier atelier = user.getAtelier();
        if (atelier == null) {
            return new TenantContext.Snapshot(null, null, List.of(), superAdmin, null);
        }
        List<Long> scope = superAdmin ? List.of() : collectScope(atelier.getId());
        return new TenantContext.Snapshot(atelier.getId(), atelier.getNom(), scope, superAdmin, atelier);
    }

    public List<Long> collectScope(Long rootId) {
        List<Long> ids = new ArrayList<>();
        ids.add(rootId);
        Deque<Long> queue = new ArrayDeque<>();
        queue.add(rootId);
        while (!queue.isEmpty()) {
            Long parentId = queue.poll();
            for (Atelier child : atelierRepository.findByParent_IdOrderByNomAsc(parentId)) {
                ids.add(child.getId());
                queue.add(child.getId());
            }
        }
        return ids;
    }
}
