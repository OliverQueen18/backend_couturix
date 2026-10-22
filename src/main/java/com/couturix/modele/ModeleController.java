package com.couturix.modele;
import com.couturix.common.api.PageResponse;
import com.couturix.domain.enums.ModeleStatut;
import com.couturix.modele.dto.*;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController @RequestMapping("/api/v1/modeles") @RequiredArgsConstructor @Tag(name="Modèles")
@PreAuthorize("hasAnyRole('SUPER_ADMIN','ADMIN','GERANT','RECEPTIONNISTE')")
public class ModeleController {
  private final ModeleService service;
  @GetMapping public PageResponse<ModeleDto> list(@RequestParam(required=false) ModeleStatut statut, @RequestParam(required=false) String q, Pageable pageable) { return service.list(statut,q,pageable); }
  @GetMapping("/{id}") public ModeleDto get(@PathVariable Long id) { return service.get(id); }
  @PostMapping public ModeleDto create(@Valid @RequestBody ModeleRequest req) { return service.create(req); }
  @PutMapping("/{id}") public ModeleDto update(@PathVariable Long id, @Valid @RequestBody ModeleRequest req) { return service.update(id, req); }
  @PostMapping("/{id}/publier") public ModeleDto publier(@PathVariable Long id) { return service.changerStatut(id, ModeleStatut.PUBLIE); }
  @PostMapping("/{id}/depublier") public ModeleDto depublier(@PathVariable Long id) { return service.changerStatut(id, ModeleStatut.DEPUBLIE); }
  @PostMapping("/{id}/archiver") public ModeleDto archiver(@PathVariable Long id) { return service.changerStatut(id, ModeleStatut.ARCHIVE); }
  @PostMapping("/{id}/images") public ModeleDto image(@PathVariable Long id, @RequestParam MultipartFile file, @RequestParam(defaultValue="false") boolean principale) { return service.addImage(id, file, principale); }
}
