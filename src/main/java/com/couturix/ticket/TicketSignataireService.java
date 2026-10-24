package com.couturix.ticket;

import com.couturix.common.exception.ApiException;
import com.couturix.common.tenant.TenantContext;
import com.couturix.ticket.dto.TicketSignataireDto;
import com.couturix.ticket.dto.TicketSignataireRequest;
import java.time.Instant;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class TicketSignataireService {

    static final int MAX_SIGNATAIRES = 6;

    private final TicketSignataireRepository repo;

    @Transactional
    public List<TicketSignataireDto> list() {
        List<TicketSignataire> rows = repo.findAllByOrderByOrdreAscIdAsc();
        if (rows.isEmpty() && TenantContext.atelier().isPresent()) {
            seedDefaults();
            rows = repo.findAllByOrderByOrdreAscIdAsc();
        }
        return rows.stream().map(this::toDto).toList();
    }

    @Transactional
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','ADMIN','GERANT')")
    public TicketSignataireDto create(TicketSignataireRequest request) {
        if (TenantContext.atelier().isEmpty()) {
            throw ApiException.badRequest("Aucun atelier associé au compte");
        }
        if (repo.count() >= MAX_SIGNATAIRES) {
            throw ApiException.badRequest("Six signataires maximum par atelier");
        }
        TicketSignataire row = new TicketSignataire();
        apply(row, request);
        if (row.getOrdre() <= 0) {
            row.setOrdre((int) repo.count() + 1);
        }
        return toDto(repo.save(row));
    }

    @Transactional
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','ADMIN','GERANT')")
    public TicketSignataireDto update(Long id, TicketSignataireRequest request) {
        TicketSignataire row = require(id);
        apply(row, request);
        row.setUpdatedAt(Instant.now());
        return toDto(repo.save(row));
    }

    @Transactional
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','ADMIN','GERANT')")
    public void delete(Long id) {
        TicketSignataire row = require(id);
        repo.delete(row);
    }

    private TicketSignataire require(Long id) {
        return repo.findById(id).orElseThrow(() -> ApiException.notFound("Signataire introuvable"));
    }

    private void seedDefaults() {
        repo.save(signataire("Le caissier", 1));
        repo.save(signataire("Le client", 2));
    }

    private TicketSignataire signataire(String libelle, int ordre) {
        TicketSignataire row = new TicketSignataire();
        row.setLibelle(libelle);
        row.setOrdre(ordre);
        row.setActif(true);
        return row;
    }

    private void apply(TicketSignataire row, TicketSignataireRequest request) {
        String libelle = normalize(request.libelle(), 80);
        if (libelle.isBlank()) {
            throw ApiException.badRequest("Indiquez la fonction du signataire");
        }
        row.setLibelle(libelle);
        String nom = normalize(request.nom(), 120);
        row.setNom(nom.isBlank() ? null : nom);
        if (request.ordre() != null) {
            row.setOrdre(Math.max(0, request.ordre()));
        }
        if (request.actif() != null) {
            row.setActif(request.actif());
        }
    }

    private TicketSignataireDto toDto(TicketSignataire row) {
        return new TicketSignataireDto(row.getId(), row.getLibelle(), row.getNom(), row.getOrdre(), row.isActif());
    }

    private static String normalize(String raw, int max) {
        if (raw == null) {
            return "";
        }
        String value = raw.trim().replaceAll("\\s+", " ");
        return value.length() <= max ? value : value.substring(0, max);
    }
}
