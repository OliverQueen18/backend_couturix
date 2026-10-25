package com.couturix.site;

import com.couturix.atelier.AtelierRepository;
import com.couturix.common.exception.ApiException;
import com.couturix.domain.enums.RoleCode;
import com.couturix.domain.enums.SiteMenuLocation;
import com.couturix.site.dto.PlatformOverviewDto;
import com.couturix.site.dto.SiteMenuDto;
import com.couturix.site.dto.SitePageDto;
import com.couturix.site.dto.SiteSettingsDto;
import com.couturix.site.dto.SiteSlideDto;
import com.couturix.site.dto.SiteSnapshotDto;
import com.couturix.user.UserRepository;
import java.text.Normalizer;
import java.time.Instant;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
public class SiteCmsService {

    private static final int MAX_SLIDES = 12;
    private static final Set<String> RESERVED_SLUGS = Set.of(
            "a-propos", "contact", "partenaires", "conditions-utilisation", "politique-confidentialite");

    private final SiteSettingsRepository settingsRepository;
    private final SiteMenuRepository menuRepository;
    private final SitePageRepository pageRepository;
    private final SiteSlideRepository slideRepository;
    private final SiteMediaService mediaService;
    private final AtelierRepository atelierRepository;
    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public SiteSnapshotDto publicSnapshot() {
        List<SitePageDto> pages = pageRepository.findByPublishedTrueOrderByTitleAsc().stream()
                .map(this::toPage)
                .toList();
        List<SiteMenuDto> menus = menuRepository.findByVisibleTrueOrderByLocationAscSortOrderAscIdAsc().stream()
                .map(this::toMenu)
                .toList();
        List<SiteSlideDto> slides = slideRepository.findByVisibleTrueOrderBySortOrderAscIdAsc().stream()
                .map(this::toSlide)
                .toList();
        return new SiteSnapshotDto(toSettings(settings()), menus, pages, slides);
    }

    @Transactional(readOnly = true)
    public SitePageDto publicPage(String slug) {
        SitePage page = pageRepository.findBySlug(normalizeSlug(slug))
                .filter(SitePage::isPublished)
                .orElseThrow(() -> ApiException.notFound("Page introuvable"));
        return toPage(page);
    }

    @Transactional(readOnly = true)
    public PlatformOverviewDto overview() {
        return new PlatformOverviewDto(
                atelierRepository.count(),
                userRepository.count(),
                userRepository.countDistinctByRoles_Code(RoleCode.CLIENT),
                pageRepository.count(),
                menuRepository.count(),
                slideRepository.count());
    }

    @Transactional(readOnly = true)
    public SiteSettingsDto settingsDto() {
        return toSettings(settings());
    }

    @Transactional
    public SiteSettingsDto saveSettings(SiteSettingsDto dto) {
        SiteSettings row = settings();
        row.setBrandName(blankTo(dto.brandName(), "CouturiX"));
        row.setTagline(trimToNull(dto.tagline()));
        row.setFooterTagline(trimToNull(dto.footerTagline()));
        row.setFooterNote(trimToNull(dto.footerNote()));
        row.setHeaderCtaLabel(trimToNull(dto.headerCtaLabel()));
        row.setHeaderCtaPath(trimToNull(dto.headerCtaPath()));
        row.setContactEmail(trimToNull(dto.contactEmail()));
        row.setContactPhones(trimToNull(dto.contactPhones()));
        row.setContactAddress(trimToNull(dto.contactAddress()));
        row.setFacebookUrl(trimToNull(dto.facebookUrl()));
        row.setInstagramUrl(trimToNull(dto.instagramUrl()));
        row.setTiktokUrl(trimToNull(dto.tiktokUrl()));
        row.setLinkedinUrl(trimToNull(dto.linkedinUrl()));
        row.setYoutubeUrl(trimToNull(dto.youtubeUrl()));
        row.setUpdatedAt(Instant.now());
        return toSettings(settingsRepository.save(row));
    }

    @Transactional(readOnly = true)
    public List<SiteMenuDto> menus() {
        return menuRepository.findAllByOrderByLocationAscSortOrderAscIdAsc().stream().map(this::toMenu).toList();
    }

    @Transactional
    public SiteMenuDto saveMenu(Long id, SiteMenuDto dto) {
        SiteMenu menu = id == null ? new SiteMenu() : menuRepository.findById(id)
                .orElseThrow(() -> ApiException.notFound("Lien introuvable"));
        SiteMenuLocation location = dto.location() == null ? SiteMenuLocation.HEADER : dto.location();
        String label = requireText(dto.label(), "Libellé obligatoire");
        String path = requireText(dto.path(), "Chemin obligatoire");
        menu.setLocation(location);
        menu.setLabel(label);
        menu.setPath(path.trim());
        menu.setSortOrder(dto.sortOrder());
        menu.setVisible(dto.visible());
        return toMenu(menuRepository.save(menu));
    }

    @Transactional
    public void deleteMenu(Long id) {
        if (!menuRepository.existsById(id)) {
            throw ApiException.notFound("Lien introuvable");
        }
        menuRepository.deleteById(id);
    }

    @Transactional(readOnly = true)
    public List<SitePageDto> pages() {
        return pageRepository.findAllByOrderByTitleAsc().stream().map(this::toPage).toList();
    }

    @Transactional
    public SitePageDto savePage(Long id, SitePageDto dto) {
        SitePage page = id == null ? new SitePage() : pageRepository.findById(id)
                .orElseThrow(() -> ApiException.notFound("Page introuvable"));
        String title = requireText(dto.title(), "Titre obligatoire");
        String previousSlug = page.getSlug();
        String slug = dto.slug() == null || dto.slug().isBlank() ? slugify(title) : normalizeSlug(dto.slug());
        if (previousSlug != null && RESERVED_SLUGS.contains(previousSlug) && !previousSlug.equals(slug)) {
            throw ApiException.badRequest("Le slug de cette page institutionnelle est fixe.");
        }
        if (slug.isBlank()) {
            throw ApiException.badRequest("Slug obligatoire");
        }
        pageRepository.findBySlug(slug).ifPresent(existing -> {
            if (id == null || !existing.getId().equals(id)) {
                throw ApiException.conflict("Ce slug est déjà utilisé");
            }
        });
        page.setSlug(slug);
        page.setTitle(title);
        page.setEyebrow(trimToNull(dto.eyebrow()));
        page.setLead(trimToNull(dto.lead()));
        page.setBody(sanitize(dto.body() == null ? "" : dto.body()));
        page.setPublished(dto.published());
        page.setUpdatedAt(Instant.now());
        return toPage(pageRepository.save(page));
    }

    @Transactional
    public void deletePage(Long id) {
        SitePage page = pageRepository.findById(id).orElseThrow(() -> ApiException.notFound("Page introuvable"));
        if (RESERVED_SLUGS.contains(page.getSlug())) {
            throw ApiException.badRequest("Cette page institutionnelle ne peut pas être supprimée. Dépubliez-la si besoin.");
        }
        pageRepository.delete(page);
    }

    @Transactional(readOnly = true)
    public List<SiteSlideDto> slides() {
        return slideRepository.findAllByOrderBySortOrderAscIdAsc().stream().map(this::toSlide).toList();
    }

    @Transactional
    public SiteSlideDto saveSlide(Long id, SiteSlideDto dto) {
        SiteSlide slide = id == null ? new SiteSlide() : slideRepository.findById(id)
                .orElseThrow(() -> ApiException.notFound("Slide introuvable"));
        if (id == null && slideRepository.count() >= MAX_SLIDES) {
            throw ApiException.badRequest("Au plus 12 images dans le slider.");
        }
        slide.setKicker(trimToNull(dto.kicker()));
        slide.setTitle(requireText(dto.title(), "Titre obligatoire"));
        slide.setBody(trimToNull(dto.body()));
        slide.setImageUrl(requireText(dto.imageUrl(), "Image obligatoire"));
        slide.setCtaLabel(trimToNull(dto.ctaLabel()));
        slide.setCtaPath(trimToNull(dto.ctaPath()));
        slide.setSortOrder(dto.sortOrder());
        slide.setVisible(dto.visible());
        slide.setShowText(dto.showText());
        return toSlide(slideRepository.save(slide));
    }

    @Transactional
    public void deleteSlide(Long id) {
        if (!slideRepository.existsById(id)) {
            throw ApiException.notFound("Slide introuvable");
        }
        slideRepository.deleteById(id);
    }

    public String uploadSlideImage(MultipartFile file) {
        return mediaService.upload(file).url();
    }

    private SiteSettings settings() {
        return settingsRepository.findById((short) 1).orElseGet(() -> {
            SiteSettings created = new SiteSettings();
            created.setId((short) 1);
            return settingsRepository.save(created);
        });
    }

    private SiteSettingsDto toSettings(SiteSettings row) {
        return new SiteSettingsDto(
                row.getBrandName(),
                row.getTagline(),
                row.getFooterTagline(),
                row.getFooterNote(),
                row.getHeaderCtaLabel(),
                row.getHeaderCtaPath(),
                row.getContactEmail(),
                row.getContactPhones(),
                row.getContactAddress(),
                row.getFacebookUrl(),
                row.getInstagramUrl(),
                row.getTiktokUrl(),
                row.getLinkedinUrl(),
                row.getYoutubeUrl());
    }

    private SiteMenuDto toMenu(SiteMenu menu) {
        return new SiteMenuDto(menu.getId(), menu.getLocation(), menu.getLabel(), menu.getPath(),
                menu.getSortOrder(), menu.isVisible());
    }

    private SitePageDto toPage(SitePage page) {
        return new SitePageDto(page.getId(), page.getSlug(), page.getTitle(), page.getEyebrow(),
                page.getLead(), page.getBody(), page.isPublished(), page.getUpdatedAt());
    }

    private SiteSlideDto toSlide(SiteSlide slide) {
        return new SiteSlideDto(
                slide.getId(),
                slide.getKicker(),
                slide.getTitle(),
                slide.getBody(),
                slide.getImageUrl(),
                slide.getCtaLabel(),
                slide.getCtaPath(),
                slide.getSortOrder(),
                slide.isVisible(),
                slide.isShowText());
    }

    private String sanitize(String html) {
        return html.replaceAll("(?is)<script[^>]*>.*?</script>", "")
                .replaceAll("(?is)<iframe[^>]*>.*?</iframe>", "");
    }

    private String normalizeSlug(String raw) {
        return slugify(raw);
    }

    private String slugify(String raw) {
        if (raw == null) {
            return "";
        }
        String normalized = Normalizer.normalize(raw.trim().toLowerCase(Locale.ROOT), Normalizer.Form.NFD)
                .replaceAll("\\p{M}+", "")
                .replaceAll("[^a-z0-9]+", "-")
                .replaceAll("(^-|-$)", "");
        return normalized;
    }

    private String requireText(String value, String message) {
        if (value == null || value.isBlank()) {
            throw ApiException.badRequest(message);
        }
        return value.trim();
    }

    private String trimToNull(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.trim();
    }

    private String blankTo(String value, String fallback) {
        return value == null || value.isBlank() ? fallback : value.trim();
    }
}
