package com.couturix.composition;
import com.couturix.composition.dto.CompositionDto;
import com.couturix.cout.CostCalculationEngine;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
@RestController @RequestMapping("/api/v1/compositions") @RequiredArgsConstructor
@PreAuthorize("hasAnyRole('SUPER_ADMIN','ADMIN','GERANT','COMPTABLE')")
public class CompositionController {
  private final CompositionService service;
  @GetMapping("/modele/{modeleId}") public List<CompositionDto> byModele(@PathVariable Long modeleId) { return service.byModele(modeleId); }
  @GetMapping("/{id}") public CompositionDto get(@PathVariable Long id) { return service.get(id); }
  @PostMapping public CompositionDto create(@RequestBody CompositionDto dto) { return service.create(dto); }
  @GetMapping("/{id}/cout") public CostCalculationEngine.CostBreakdown cout(@PathVariable Long id) { return service.cout(id); }
}
