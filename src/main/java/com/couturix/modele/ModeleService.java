package com.couturix.modele;
import com.couturix.common.api.PageResponse;
import com.couturix.common.exception.ApiException;
import com.couturix.domain.enums.ModeleStatut;
import com.couturix.modele.dto.*;
import com.couturix.storage.FileStorageService;
import java.text.Normalizer; import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service; import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service @RequiredArgsConstructor
public class ModeleService {
  private final ModeleRepository modeleRepository;
  private final CategorieModeleRepository categorieRepository;
  private final FileStorageService fileStorageService;

  @Transactional(readOnly=true)
  public PageResponse<ModeleDto> list(ModeleStatut statut, String q, Pageable pageable) {
    var spec = (org.springframework.data.jpa.domain.Specification<Modele>) (root, query, cb) -> {
      var p = cb.conjunction();
      if (statut != null) p = cb.and(p, cb.equal(root.get("statut"), statut));
      if (q != null && !q.isBlank()) p = cb.and(p, cb.like(cb.lower(root.get("nom")), "%" + q.toLowerCase() + "%"));
      return p;
    };
    return PageResponse.of(modeleRepository.findAll(spec, pageable).map(this::toDto));
  }

  @Transactional(readOnly=true) public ModeleDto get(Long id) { return toDto(require(id)); }

  @Transactional @PreAuthorize("hasAnyRole('SUPER_ADMIN','ADMIN','GERANT')")
  public ModeleDto create(ModeleRequest req) {
    Modele m = new Modele(); apply(m, req); m.setStatut(ModeleStatut.BROUILLON);
    return toDto(modeleRepository.save(m));
  }

  @Transactional @PreAuthorize("hasAnyRole('SUPER_ADMIN','ADMIN','GERANT')")
  public ModeleDto update(Long id, ModeleRequest req) { Modele m = require(id); apply(m, req); return toDto(modeleRepository.save(m)); }

  @Transactional @PreAuthorize("hasAnyRole('SUPER_ADMIN','ADMIN','GERANT')")
  public ModeleDto changerStatut(Long id, ModeleStatut statut) { Modele m = require(id); m.setStatut(statut); return toDto(modeleRepository.save(m)); }

  @Transactional @PreAuthorize("hasAnyRole('SUPER_ADMIN','ADMIN','GERANT')")
  public ModeleDto addImage(Long id, MultipartFile file, boolean principale) {
    Modele m = require(id);
    ModeleImage img = new ModeleImage(); img.setModele(m); img.setUrl(fileStorageService.store(file, "modeles"));
    img.setPrincipale(principale); img.setOrdre(m.getImages().size());
    m.getImages().add(img); return toDto(modeleRepository.save(m));
  }

  public List<CategorieDto> categories() {
    return categorieRepository.findAll().stream().map(c -> new CategorieDto(c.getId(), c.getNom(), c.getSlug(), c.getDescription(), c.getImageUrl(), c.isActif())).toList();
  }

  @Transactional @PreAuthorize("hasAnyRole('SUPER_ADMIN','ADMIN','GERANT')")
  public CategorieDto saveCategorie(CategorieDto dto) {
    CategorieModele c = dto.id()==null ? new CategorieModele() : categorieRepository.findById(dto.id()).orElseThrow(() -> ApiException.notFound("Catégorie introuvable"));
    c.setNom(dto.nom()); c.setSlug(slugify(dto.nom())); c.setDescription(dto.description()); c.setActif(dto.actif());
    CategorieModele saved = categorieRepository.save(c);
    return new CategorieDto(saved.getId(), saved.getNom(), saved.getSlug(), saved.getDescription(), saved.getImageUrl(), saved.isActif());
  }

  public Modele require(Long id) { return modeleRepository.findById(id).orElseThrow(() -> ApiException.notFound("Modèle introuvable")); }

  private void apply(Modele m, ModeleRequest req) {
    m.setNom(req.nom()); m.setDescription(req.description()); m.setPrixIndicatif(req.prixIndicatif());
    m.setTissuRecommande(req.tissuRecommande()); m.setTempsFabricationJours(req.tempsFabricationJours());
    if (req.categorieId()!=null) m.setCategorie(categorieRepository.findById(req.categorieId()).orElseThrow(() -> ApiException.notFound("Catégorie introuvable")));
  }

  public ModeleDto toDto(Modele m) {
    return new ModeleDto(m.getId(), m.getCategorie()==null?null:m.getCategorie().getId(), m.getCategorie()==null?null:m.getCategorie().getNom(),
      m.getNom(), m.getDescription(), m.getPrixIndicatif(), m.getTissuRecommande(), m.getTempsFabricationJours(), m.getStatut(),
      m.getImages().stream().map(ModeleImage::getUrl).toList());
  }

  private String slugify(String nom) {
    String n = Normalizer.normalize(nom, Normalizer.Form.NFD).replaceAll("\\p{M}", "").toLowerCase().replaceAll("[^a-z0-9]+","-");
    return n.replaceAll("^-|-$", "");
  }
}
