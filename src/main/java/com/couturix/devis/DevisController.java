package com.couturix.devis;
import java.util.List; import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
@RestController @RequestMapping("/api/v1/devis") @RequiredArgsConstructor
@PreAuthorize("hasAnyRole('SUPER_ADMIN','ADMIN','GERANT','RECEPTIONNISTE')")
public class DevisController {
  private final DevisService service;
  @GetMapping public List<Devis> list() { return service.list(); }
  @PostMapping public Devis create(@RequestBody Devis d) { return service.create(d); }
  @PostMapping("/{id}/accepter") public Devis accepter(@PathVariable Long id) { return service.accepter(id); }
  @PostMapping("/depuis-simulation") public Devis fromSim(@RequestBody Map<String, Long> body) { return service.fromSimulation(body.get("simulationId"), body.get("clientId")); }
}
