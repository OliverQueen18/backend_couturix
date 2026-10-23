package com.couturix.tailleur;

import com.couturix.common.exception.ApiException;
import com.couturix.tailleur.dto.TailleurDto;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class TailleurService {
    private final TailleurRepository repo;
    private final SpecialiteRepository specialiteRepository;

    @Transactional(readOnly = true)
    public List<TailleurDto> list() {
        return repo.findAll().stream().map(this::toDto).toList();
    }

    @Transactional(readOnly = true)
    public TailleurDto get(Long id) {
        return toDto(require(id));
    }

    public Tailleur require(Long id) {
        return repo.findById(id).orElseThrow(() -> ApiException.notFound("Tailleur introuvable"));
    }

    public Tailleur find(Long id) {
        return require(id);
    }

    public Long findByCurrentUser() {
        Long userId = com.couturix.security.SecurityUtils.currentUserId();
        return repo.findByUserId(userId).map(Tailleur::getId).orElse(null);
    }

    @Transactional
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','ADMIN','GERANT')")
    public TailleurDto save(TailleurDto dto) {
        return save(dto.id(), dto);
    }

    @Transactional
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','ADMIN','GERANT')")
    public TailleurDto save(Long id, TailleurDto dto) {
        Tailleur t = id == null ? new Tailleur() : require(id);
        t.setNom(dto.nom());
        t.setPrenom(dto.prenom());
        t.setTelephone(dto.telephone());
        t.setEmail(dto.email());
        t.setModeRemuneration(dto.modeRemuneration());
        t.setSalaireFixe(dto.salaireFixe());
        t.setTauxPiece(dto.tauxPiece());
        t.setPourcentage(dto.pourcentage());
        t.setActif(dto.actif());
        if (dto.specialiteIds() != null) {
            Set<Specialite> selected = new HashSet<>();
            specialiteRepository.findAllById(dto.specialiteIds()).forEach(selected::add);
            t.setSpecialites(selected);
        } else if (dto.specialites() != null) {
            t.setSpecialites(dto.specialites().stream()
                    .map(this::requireSpecialite)
                    .collect(Collectors.toSet()));
        }
        return toDto(repo.save(t));
    }

    @Transactional(readOnly = true)
    public List<Specialite> specialites() {
        return specialiteRepository.findAll(Sort.by(Sort.Direction.ASC, "libelle"));
    }

    @Transactional
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','ADMIN','GERANT')")
    public Specialite createSpecialite(String libelle) {
        String normalized = normalizeLibelle(libelle);
        if (normalized.isBlank()) {
            throw ApiException.badRequest("Indiquez le nom de la spécialité");
        }
        return specialiteRepository.findByLibelleIgnoreCase(normalized).orElseGet(() -> {
            Specialite created = new Specialite();
            created.setLibelle(normalized);
            return specialiteRepository.save(created);
        });
    }

    private Specialite requireSpecialite(String libelle) {
        return createSpecialite(libelle);
    }

    private static String normalizeLibelle(String libelle) {
        return libelle == null ? "" : libelle.trim().replaceAll("\\s+", " ");
    }

    public TailleurDto toDto(Tailleur t) {
        Set<Specialite> specs = t.getSpecialites() == null ? Set.of() : t.getSpecialites();
        return new TailleurDto(
                t.getId(),
                t.getNom(),
                t.getPrenom(),
                t.getTelephone(),
                t.getEmail(),
                t.getModeRemuneration(),
                t.getSalaireFixe(),
                t.getTauxPiece(),
                t.getPourcentage(),
                t.isActif(),
                specs.stream().map(Specialite::getLibelle).collect(Collectors.toSet()),
                specs.stream().map(Specialite::getId).collect(Collectors.toSet()));
    }
}
