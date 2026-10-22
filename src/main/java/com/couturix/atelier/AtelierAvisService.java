package com.couturix.atelier;

import com.couturix.atelier.dto.AtelierAvisDto;
import com.couturix.atelier.dto.AvisRequest;
import com.couturix.atelier.dto.RatingStats;
import com.couturix.client.Client;
import com.couturix.client.ClientRepository;
import com.couturix.common.exception.ApiException;
import com.couturix.security.SecurityUtils;
import java.time.Instant;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AtelierAvisService {

    private final AtelierAvisRepository avisRepository;
    private final AtelierRepository atelierRepository;
    private final ClientRepository clientRepository;

    @Transactional(readOnly = true)
    public List<AtelierAvisDto> list(String slug) {
        Atelier atelier = requirePublic(slug);
        Long mine = currentClientId();
        return avisRepository.findByAtelierIdOrderByCreatedAtDesc(atelier.getId()).stream()
                .map(avis -> toDto(avis, mine))
                .toList();
    }

    @Transactional(readOnly = true)
    public AtelierAvisDto mine(String slug) {
        Atelier atelier = requirePublic(slug);
        Client client = requireClient();
        return avisRepository.findByAtelierIdAndClientId(atelier.getId(), client.getId())
                .map(avis -> toDto(avis, client.getId()))
                .orElse(null);
    }

    @Transactional
    @PreAuthorize("hasRole('CLIENT')")
    public AtelierAvisDto upsert(String slug, AvisRequest request) {
        Atelier atelier = requirePublic(slug);
        Client client = requireClient();
        AtelierAvis avis = avisRepository.findByAtelierIdAndClientId(atelier.getId(), client.getId())
                .orElseGet(AtelierAvis::new);
        avis.setAtelier(atelier);
        avis.setClient(client);
        avis.setNote(request.note());
        avis.setCommentaire(request.commentaire() == null || request.commentaire().isBlank()
                ? null : request.commentaire().trim());
        avis.setUpdatedAt(Instant.now());
        if (avis.getCreatedAt() == null) {
            avis.setCreatedAt(Instant.now());
        }
        return toDto(avisRepository.save(avis), client.getId());
    }

    @Transactional(readOnly = true)
    public Map<Long, RatingStats> statsByAtelierIds(Collection<Long> ids) {
        Map<Long, RatingStats> map = new HashMap<>();
        if (ids == null || ids.isEmpty()) {
            return map;
        }
        for (Object[] row : avisRepository.aggregateByAtelierIds(ids)) {
            Long id = (Long) row[0];
            double moyenne = row[1] == null ? 0 : ((Number) row[1]).doubleValue();
            long total = row[2] == null ? 0 : ((Number) row[2]).longValue();
            map.put(id, new RatingStats(Math.round(moyenne * 10.0) / 10.0, total));
        }
        return map;
    }

    private Atelier requirePublic(String slug) {
        return atelierRepository.findBySlug(slug)
                .filter(Atelier::isActif)
                .orElseThrow(() -> ApiException.notFound("Atelier introuvable"));
    }

    private Client requireClient() {
        return clientRepository.findByUserId(SecurityUtils.currentUserId())
                .orElseThrow(() -> ApiException.forbidden("Aucun profil client associé"));
    }

    private Long currentClientId() {
        return SecurityUtils.currentUserOptional()
                .flatMap(user -> clientRepository.findByUserId(user.getId()))
                .map(Client::getId)
                .orElse(null);
    }

    private AtelierAvisDto toDto(AtelierAvis avis, Long mineId) {
        Client client = avis.getClient();
        String auteur = client.getPrenom();
        if (client.getNom() != null && !client.getNom().isBlank()) {
            auteur = auteur + " " + client.getNom().substring(0, 1).toUpperCase() + ".";
        }
        return new AtelierAvisDto(
                avis.getId(),
                auteur,
                avis.getNote(),
                avis.getCommentaire(),
                avis.getCreatedAt(),
                mineId != null && mineId.equals(client.getId())
        );
    }
}
