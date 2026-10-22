package com.couturix.fournisseur;

import com.couturix.common.exception.ApiException;
import com.couturix.fournisseur.dto.FournisseurRequest;
import java.time.Instant;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class FournisseurService {

    private final FournisseurRepository repo;

    @Transactional(readOnly = true)
    public List<Fournisseur> list() {
        return repo.findAll();
    }

    @Transactional
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','ADMIN','GERANT','MAGASINIER')")
    public Fournisseur create(FournisseurRequest request) {
        Fournisseur fournisseur = new Fournisseur();
        apply(fournisseur, request);
        return repo.save(fournisseur);
    }

    @Transactional
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','ADMIN','GERANT','MAGASINIER')")
    public Fournisseur update(Long id, FournisseurRequest request) {
        Fournisseur fournisseur = repo.findById(id)
                .orElseThrow(() -> ApiException.notFound("Fournisseur introuvable"));
        apply(fournisseur, request);
        fournisseur.setUpdatedAt(Instant.now());
        return repo.save(fournisseur);
    }

    @Transactional
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','ADMIN','GERANT')")
    public void delete(Long id) {
        if (!repo.existsById(id)) {
            throw ApiException.notFound("Fournisseur introuvable");
        }
        repo.deleteById(id);
    }

    private void apply(Fournisseur fournisseur, FournisseurRequest request) {
        fournisseur.setNom(request.nom());
        fournisseur.setTelephone(request.telephone());
        fournisseur.setEmail(request.email());
        fournisseur.setAdresse(request.adresse());
        fournisseur.setNotes(request.notes());
        if (request.actif() != null) {
            fournisseur.setActif(request.actif());
        }
    }
}
