package com.couturix.simulateur;
import com.couturix.cout.CostCalculationEngine;
import com.couturix.devis.Devis;
import com.couturix.devis.DevisService;
import com.couturix.simulateur.dto.SimulationRequest;
import java.util.List; import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
@RestController @RequiredArgsConstructor
@PreAuthorize("hasAnyRole('SUPER_ADMIN','ADMIN','GERANT','COMPTABLE')")
public class SimulateurController {
  private final SimulateurService service;
  private final DevisService devisService;
  @PostMapping("/api/v1/simulateur/calculer") public CostCalculationEngine.ProfitResult calc(@RequestBody SimulationRequest req) { return service.calculer(req); }
  @PostMapping("/api/v1/simulateur/sauvegarder") public Simulation save(@RequestBody SimulationRequest req) { return service.sauvegarder(req); }
  @GetMapping("/api/v1/simulations") public List<Simulation> list() { return service.list(); }
  @PostMapping("/api/v1/simulateur/{id}/devis") public Devis toDevis(@PathVariable Long id, @RequestBody Map<String, Long> body) { return devisService.fromSimulation(id, body.get("clientId")); }
}
