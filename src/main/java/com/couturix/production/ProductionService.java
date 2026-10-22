package com.couturix.production;

import com.couturix.commande.Commande;
import com.couturix.common.exception.ApiException;
import com.couturix.domain.enums.CommandeStatut;
import com.couturix.domain.enums.ProductionStatut;
import com.couturix.production.dto.ProductionDto;
import com.couturix.production.dto.ProductionEtapeDto;
import com.couturix.production.dto.ProductionEtapeRequest;
import com.couturix.security.SecurityUtils;
import com.couturix.tailleur.Tailleur;
import com.couturix.tailleur.TailleurRepository;
import com.couturix.user.User;
import com.couturix.user.UserRepository;
import java.time.Instant;
import java.util.List;
import java.util.Locale;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ProductionService {

    private final ProductionRepository productionRepository;
    private final ProductionEtapeRepository etapeRepository;
    private final TailleurRepository tailleurRepository;
    private final UserRepository userRepository;

    @Transactional
    public Production createIfMissing(Commande commande) {
        return productionRepository.findByCommandeId(commande.getId()).orElseGet(() -> {
            Production production = new Production();
            production.setCommande(commande);
            production.setTailleur(commande.getTailleur());
            production.setStatut(ProductionStatut.EN_ATTENTE);
            production.setDateDebut(Instant.now());
            productionRepository.save(production);
            appendEtape(production, commande.getStatut().name(), "Production créée");
            return production;
        });
    }

    @Transactional
    public void onCommandeStatut(Commande commande, String commentaire) {
        Production production = createIfMissing(commande);
        if (commande.getTailleur() != null) {
            production.setTailleur(commande.getTailleur());
        }
        production.setStatut(mapStatut(commande.getStatut()));
        if (commande.getStatut() == CommandeStatut.LIVREE || commande.getStatut() == CommandeStatut.PRETE) {
            production.setDateFin(Instant.now());
        }
        productionRepository.save(production);
        appendEtape(production, commande.getStatut().name(), commentaire);
    }

    @Transactional
    public void assignTailleur(Commande commande, Tailleur tailleur) {
        Production production = productionRepository.findByCommandeId(commande.getId()).orElse(null);
        if (production != null) {
            production.setTailleur(tailleur);
            productionRepository.save(production);
        }
    }

    @Transactional(readOnly = true)
    public Page<ProductionDto> search(Long tailleurId, ProductionStatut statut, Pageable pageable) {
        Long scopedTailleur = tailleurId;
        if (SecurityUtils.hasRole("TAILLEUR") && !SecurityUtils.hasRole("ADMIN") && !SecurityUtils.hasRole("SUPER_ADMIN")
                && !SecurityUtils.hasRole("GERANT")) {
            scopedTailleur = tailleurRepository.findByUserId(SecurityUtils.currentUserId())
                    .map(Tailleur::getId)
                    .orElse(-1L);
        }
        Long filterTailleur = scopedTailleur;
        Specification<Production> spec = (root, query, cb) -> {
            var predicate = cb.conjunction();
            if (filterTailleur != null) {
                predicate = cb.and(predicate, cb.equal(root.get("tailleur").get("id"), filterTailleur));
            }
            if (statut != null) {
                predicate = cb.and(predicate, cb.equal(root.get("statut"), statut));
            }
            return predicate;
        };
        return productionRepository.findAll(spec, pageable).map(this::toDto);
    }

    @Transactional
    public ProductionDto addEtape(Long id, ProductionEtapeRequest request) {
        Production production = productionRepository.findById(id)
                .orElseThrow(() -> ApiException.notFound("Production introuvable"));
        assertOwnProduction(production);
        appendEtape(production, request.etape().toUpperCase(Locale.ROOT), request.commentaire());
        return toDto(production);
    }

    public ProductionDto toDto(Production p) {
        List<ProductionEtapeDto> etapes = p.getEtapes().stream()
                .map(e -> new ProductionEtapeDto(e.getId(), e.getEtape(), e.getCommentaire(), e.getCreatedAt()))
                .toList();
        return new ProductionDto(
                p.getId(),
                p.getCommande().getId(),
                p.getCommande().getNumero(),
                p.getTailleur() == null ? null : p.getTailleur().getId(),
                p.getTailleur() == null ? null : p.getTailleur().nomComplet(),
                p.getDateDebut(),
                p.getDateFin(),
                p.getStatut(),
                etapes,
                p.getCreatedAt()
        );
    }

    private void appendEtape(Production production, String etape, String commentaire) {
        ProductionEtape entity = new ProductionEtape();
        entity.setProduction(production);
        entity.setEtape(etape);
        entity.setCommentaire(commentaire);
        SecurityUtils.currentUserOptional().ifPresent(principal -> {
            User user = userRepository.findById(principal.getId()).orElse(null);
            entity.setUser(user);
        });
        production.getEtapes().add(entity);
        etapeRepository.save(entity);
    }

    private void assertOwnProduction(Production production) {
        if (SecurityUtils.hasRole("TAILLEUR") && !SecurityUtils.hasRole("ADMIN") && !SecurityUtils.hasRole("SUPER_ADMIN")
                && !SecurityUtils.hasRole("GERANT")) {
            Long userId = SecurityUtils.currentUserId();
            if (production.getTailleur() == null || production.getTailleur().getUser() == null
                    || !production.getTailleur().getUser().getId().equals(userId)) {
                throw ApiException.forbidden("Production non autorisée");
            }
        }
    }

    private static ProductionStatut mapStatut(CommandeStatut statut) {
        return switch (statut) {
            case BROUILLON, CONFIRMEE, ANNULEE -> ProductionStatut.EN_ATTENTE;
            case PRETE, LIVREE -> ProductionStatut.TERMINEE;
            default -> ProductionStatut.EN_COURS;
        };
    }
}
