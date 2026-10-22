package com.couturix.tailleur;

import com.couturix.common.exception.ApiException;
import com.couturix.tailleur.dto.TailleurDto;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class TailleurService {
    private final TailleurRepository repo;
    private final SpecialiteRepository specialiteRepository;

    public List<TailleurDto> list() {
        return repo.findAll().stream().map(this::toDto).toList();
    }

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
        if (dto.specialiteIds() != null && !dto.specialiteIds().isEmpty()) {
            Set<Specialite> selected = new HashSet<>();
            specialiteRepository.findAllById(dto.specialiteIds()).forEach(selected::add);
            t.setSpecialites(selected);
        } else if (dto.specialites() != null) {
            t.setSpecialites(dto.specialites().stream()
                    .map(n -> specialiteRepository.findAll().stream()
                            .filter(s -> s.getLibelle().equals(n))
                            .findFirst()
                            .orElseGet(() -> {
                                Specialite s = new Specialite();
                                s.setLibelle(n);
                                return specialiteRepository.save(s);
                            }))
                    .collect(Collectors.toSet()));
        }
        return toDto(repo.save(t));
    }

    public List<Specialite> specialites() {
        return specialiteRepository.findAll();
    }

    public TailleurDto toDto(Tailleur t) {
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
                t.getSpecialites().stream().map(Specialite::getLibelle).collect(Collectors.toSet()),
                t.getSpecialites().stream().map(Specialite::getId).collect(Collectors.toSet()));
    }
}
