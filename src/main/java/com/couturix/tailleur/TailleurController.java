package com.couturix.tailleur;
import com.couturix.tailleur.dto.TailleurDto;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
@RestController @RequestMapping("/api/v1") @RequiredArgsConstructor
@PreAuthorize("hasAnyRole('SUPER_ADMIN','ADMIN','GERANT','COMPTABLE','RECEPTIONNISTE')")
public class TailleurController {
  private final TailleurService service;
  @GetMapping("/tailleurs") public List<TailleurDto> list() { return service.list(); }
  @GetMapping("/tailleurs/{id}") public TailleurDto get(@PathVariable Long id) { return service.get(id); }
  @PostMapping("/tailleurs") public TailleurDto create(@RequestBody TailleurDto dto) { return service.save(dto); }
  @PutMapping("/tailleurs/{id}") public TailleurDto update(@PathVariable Long id, @RequestBody TailleurDto dto) { return service.save(id, dto); }
  @GetMapping("/specialites") public List<Specialite> specialites() { return service.specialites(); }
}
