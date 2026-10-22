package com.couturix.atelier;

import com.couturix.abonnement.AbonnementService;
import com.couturix.atelier.dto.AtelierDto;
import com.couturix.atelier.dto.AtelierPublicDto;
import com.couturix.atelier.dto.AtelierRequest;
import com.couturix.atelier.dto.RatingStats;
import com.couturix.common.exception.ApiException;
import com.couturix.common.tenant.TenantContext;
import com.couturix.storage.FileStorageService;
import java.text.Normalizer;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
public class AtelierService {

    private final AtelierRepository atelierRepository;
    private final TenantService tenantService;
    private final FileStorageService fileStorageService;
    private final AtelierAvisService avisService;
    private final AbonnementService abonnementService;

    @Transactional(readOnly = true)
    public List<AtelierDto> tree() {
        if (TenantContext.current().map(TenantContext.Snapshot::bypass).orElse(false)) {
            return atelierRepository.findByParentIsNullOrderByNomAsc().stream().map(this::toTree).toList();
        }
        Long currentId = TenantContext.current().map(TenantContext.Snapshot::atelierId)
                .orElseThrow(() -> ApiException.forbidden("Aucun atelier associé"));
        Atelier root = require(currentId);
        return List.of(toTree(root));
    }

    @Transactional(readOnly = true)
    public AtelierDto get(Long id) {
        return toTree(requireAccessible(id));
    }

    @Transactional(readOnly = true)
    public AtelierDto current() {
        Long currentId = TenantContext.current().map(TenantContext.Snapshot::atelierId)
                .orElseThrow(() -> ApiException.badRequest("Aucun atelier courant"));
        return toDtoFlat(require(currentId), List.of());
    }

    @Transactional(readOnly = true)
    public List<AtelierPublicDto> publicList(String q) {
        List<Atelier> ateliers = atelierRepository.findByActifTrueAndParentIsNullOrderByNomAsc().stream()
                .filter(atelier -> !"atelier-principal".equals(atelier.getSlug()))
                .filter(atelier -> matchesSearch(atelier, q))
                .toList();
        Map<Long, RatingStats> stats = avisService.statsByAtelierIds(
                ateliers.stream().map(Atelier::getId).toList());
        List<AtelierPublicDto> ranked = ateliers.stream()
                .sorted(publicComparator(stats))
                .map(atelier -> toPublic(atelier, stats.getOrDefault(atelier.getId(), RatingStats.empty()), false))
                .toList();
        return markTopRated(ranked);
    }

    @Transactional(readOnly = true)
    public AtelierPublicDto publicBySlug(String slug) {
        return publicList(null).stream()
                .filter(row -> row.slug().equals(slug))
                .findFirst()
                .orElseGet(() -> {
                    Atelier atelier = atelierRepository.findBySlug(slug)
                            .filter(Atelier::isActif)
                            .orElseThrow(() -> ApiException.notFound("Atelier introuvable"));
                    Map<Long, RatingStats> stats = avisService.statsByAtelierIds(List.of(atelier.getId()));
                    return toPublic(atelier, stats.getOrDefault(atelier.getId(), RatingStats.empty()), false);
                });
    }

    @Transactional
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','ADMIN','GERANT')")
    public AtelierDto create(AtelierRequest request) {
        abonnementService.assertPeutCreer();
        Atelier atelier = new Atelier();
        apply(atelier, request);
        if (request.parentId() != null) {
            Atelier parent = requireAccessible(request.parentId());
            atelier.setParent(parent);
        } else if (!TenantContext.current().map(TenantContext.Snapshot::bypass).orElse(false)) {
            Long currentId = TenantContext.current().map(TenantContext.Snapshot::atelierId)
                    .orElseThrow(() -> ApiException.forbidden("Aucun atelier parent"));
            atelier.setParent(require(currentId));
        }
        atelier.setSlug(uniqueSlug(request.nom()));
        return toDtoFlat(atelierRepository.save(atelier), List.of());
    }

    @Transactional
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','ADMIN','GERANT')")
    public AtelierDto update(Long id, AtelierRequest request) {
        Atelier atelier = requireAccessible(id);
        apply(atelier, request);
        if (request.parentId() != null && !request.parentId().equals(atelier.getId())) {
            if (tenantService.collectScope(atelier.getId()).contains(request.parentId())) {
                throw ApiException.badRequest("Un atelier ne peut pas être rattaché à l'un de ses sous-ateliers");
            }
            atelier.setParent(requireAccessible(request.parentId()));
        }
        if (!atelier.getNom().equalsIgnoreCase(request.nom())) {
            atelier.setSlug(uniqueSlug(request.nom()));
        }
        return toDtoFlat(atelierRepository.save(atelier), List.of());
    }

    @Transactional
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','ADMIN','GERANT')")
    public AtelierDto uploadLogo(Long id, MultipartFile file) {
        Atelier atelier = requireAccessible(id);
        atelier.setLogoUrl(fileStorageService.store(file, "ateliers"));
        return toDtoFlat(atelierRepository.save(atelier), List.of());
    }

    public Atelier require(Long id) {
        return atelierRepository.findById(id).orElseThrow(() -> ApiException.notFound("Atelier introuvable"));
    }

    private Atelier requireAccessible(Long id) {
        Atelier atelier = require(id);
        TenantContext.Snapshot snapshot = TenantContext.current().orElse(null);
        if (snapshot == null || snapshot.bypass()) {
            return atelier;
        }
        if (!snapshot.scopeIds().contains(id)) {
            throw ApiException.forbidden("Accès refusé à cet atelier");
        }
        return atelier;
    }

    private void apply(Atelier atelier, AtelierRequest request) {
        atelier.setNom(request.nom().trim());
        atelier.setDescription(request.description());
        atelier.setAdresse(request.adresse());
        atelier.setVille(request.ville());
        atelier.setPays(blankToDefault(request.pays(), "Mali"));
        atelier.setTelephone(request.telephone());
        atelier.setEmail(request.email());
        atelier.setSiteWeb(request.siteWeb());
        atelier.setRccm(request.rccm());
        atelier.setNif(request.nif());
        atelier.setSigle(request.sigle());
        atelier.setLatitude(request.latitude());
        atelier.setLongitude(request.longitude());
        atelier.setDevise(blankToDefault(request.devise(), "XOF"));
        if (request.actif() != null) {
            atelier.setActif(request.actif());
        }
    }

    public String allocateSlug(String nom) {
        String base = slugify(nom);
        String slug = base;
        int i = 2;
        while (atelierRepository.existsBySlug(slug)) {
            slug = base + "-" + i++;
        }
        return slug;
    }

    private String uniqueSlug(String nom) {
        return allocateSlug(nom);
    }

    static String slugify(String nom) {
        String normalized = Normalizer.normalize(nom, Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "")
                .toLowerCase(Locale.ROOT)
                .replaceAll("[^a-z0-9]+", "-")
                .replaceAll("(^-|-$)", "");
        return normalized.isBlank() ? "atelier" : normalized;
    }

    private String blankToDefault(String value, String fallback) {
        return value == null || value.isBlank() ? fallback : value.trim();
    }

    private AtelierDto toTree(Atelier atelier) {
        List<AtelierDto> children = atelierRepository.findByParent_IdOrderByNomAsc(atelier.getId()).stream()
                .map(this::toTree)
                .toList();
        return toDtoFlat(atelier, children);
    }

    public AtelierDto toDtoFlat(Atelier atelier, List<AtelierDto> children) {
        return new AtelierDto(
                atelier.getId(),
                atelier.getParent() == null ? null : atelier.getParent().getId(),
                atelier.getNom(),
                atelier.getSlug(),
                atelier.getDescription(),
                atelier.getAdresse(),
                atelier.getVille(),
                atelier.getPays(),
                atelier.getTelephone(),
                atelier.getEmail(),
                atelier.getSiteWeb(),
                atelier.getRccm(),
                atelier.getNif(),
                atelier.getSigle(),
                atelier.getLatitude(),
                atelier.getLongitude(),
                atelier.getLogoUrl(),
                atelier.getDevise(),
                atelier.isActif(),
                children
        );
    }

    private AtelierPublicDto toPublic(Atelier atelier, RatingStats stats, boolean mieuxNote) {
        return new AtelierPublicDto(
                atelier.getNom(),
                atelier.getSlug(),
                atelier.getDescription(),
                atelier.getVille(),
                atelier.getPays(),
                atelier.getLogoUrl(),
                atelier.getTelephone(),
                atelier.getSiteWeb(),
                atelier.getFacebook(),
                atelier.getInstagram(),
                atelier.getWhatsapp(),
                stats.moyenne(),
                stats.total(),
                mieuxNote
        );
    }

    private boolean matchesSearch(Atelier atelier, String q) {
        if (q == null || q.isBlank()) {
            return true;
        }
        String needle = q.toLowerCase(Locale.ROOT).trim();
        return contains(atelier.getNom(), needle)
                || contains(atelier.getSigle(), needle)
                || contains(atelier.getVille(), needle)
                || contains(atelier.getPays(), needle)
                || contains(atelier.getDescription(), needle)
                || contains(atelier.getAdresse(), needle);
    }

    private boolean contains(String value, String needle) {
        return value != null && value.toLowerCase(Locale.ROOT).contains(needle);
    }

    private Comparator<Atelier> publicComparator(Map<Long, RatingStats> stats) {
        return Comparator
                .<Atelier>comparingDouble(atelier -> {
                    RatingStats rating = stats.getOrDefault(atelier.getId(), RatingStats.empty());
                    if (rating.total() == 0) {
                        return 0;
                    }
                    return rating.moyenne() + Math.log1p(rating.total()) / 10.0;
                })
                .reversed()
                .thenComparing(Atelier::getNom, String.CASE_INSENSITIVE_ORDER);
    }

    private List<AtelierPublicDto> markTopRated(List<AtelierPublicDto> ranked) {
        java.util.ArrayList<AtelierPublicDto> result = new java.util.ArrayList<>();
        int highlighted = 0;
        for (AtelierPublicDto row : ranked) {
            boolean featured = row.nombreAvis() > 0 && highlighted < 3;
            if (featured) {
                highlighted++;
            }
            result.add(new AtelierPublicDto(
                    row.nom(), row.slug(), row.description(), row.ville(), row.pays(), row.logoUrl(),
                    row.telephone(), row.siteWeb(), row.facebook(), row.instagram(), row.whatsapp(),
                    row.noteMoyenne(), row.nombreAvis(), featured
            ));
        }
        return List.copyOf(result);
    }
}
