package com.couturix.fournisseur;

import com.couturix.common.audit.AuditService;
import com.couturix.common.exception.ApiException;
import com.couturix.fournisseur.dto.FournisseurDto;
import com.couturix.fournisseur.dto.FournisseurRequest;
import com.couturix.security.SecurityUtils;
import java.util.Locale;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class FournisseurService {

    private final FournisseurRepository repository;
    private final AuditService auditService;

    @Transactional(readOnly = true)
    public Page<FournisseurDto> search(String q, Pageable pageable) {
        Specification<Fournisseur> spec = (root, query, cb) -> {
            if (q == null || q.isBlank()) {
                return cb.conjunction();
            }
            String like = "%" + q.toLowerCase(Locale.ROOT) + "%";
            return cb.or(
                    cb.like(cb.lower(root.get("nom")), like),
                    cb.like(cb.lower(root.get("telephone")), like),
                    cb.like(cb.lower(root.get("email")), like)
            );
        };
        return repository.findAll(spec, pageable).map(this::toDto);
    }

    @Transactional(readOnly = true)
    public FournisseurDto get(Long id) {
        return toDto(find(id));
    }

    @Transactional
    public FournisseurDto create(FournisseurRequest request) {
        Fournisseur f = new Fournisseur();
        apply(f, request);
        repository.save(f);
        auditService.log(SecurityUtils.currentUserId(), "CREATE", "FOURNISSEUR", f.getId(), f.getNom());
        return toDto(f);
    }

    @Transactional
    public FournisseurDto update(Long id, FournisseurRequest request) {
        Fournisseur f = find(id);
        apply(f, request);
        repository.save(f);
        auditService.log(SecurityUtils.currentUserId(), "UPDATE", "FOURNISSEUR", id, f.getNom());
        return toDto(f);
    }

    @Transactional
    public void delete(Long id) {
        Fournisseur f = find(id);
        f.setActif(false);
        repository.save(f);
        auditService.log(SecurityUtils.currentUserId(), "DELETE", "FOURNISSEUR", id, null);
    }

    public Fournisseur find(Long id) {
        return repository.findById(id).orElseThrow(() -> ApiException.notFound("Fournisseur introuvable"));
    }

    public FournisseurDto toDto(Fournisseur f) {
        return new FournisseurDto(f.getId(), f.getNom(), f.getTelephone(), f.getEmail(), f.getAdresse(), f.getNotes(), f.isActif());
    }

    private void apply(Fournisseur f, FournisseurRequest request) {
        f.setNom(request.nom());
        f.setTelephone(request.telephone());
        f.setEmail(request.email());
        f.setAdresse(request.adresse());
        f.setNotes(request.notes());
        if (request.actif() != null) {
            f.setActif(request.actif());
        }
    }
}
