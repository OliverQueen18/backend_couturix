package com.couturix.paie;
import java.math.BigDecimal; import java.time.LocalDate; import java.util.List; import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
@RestController @RequiredArgsConstructor
@PreAuthorize("hasAnyRole('SUPER_ADMIN','ADMIN','GERANT','COMPTABLE')")
public class PaieController {
  private final PaieService service;
  @GetMapping("/api/v1/paies") public List<Paie> paies() { return service.list(); }
  @GetMapping("/api/v1/avances") public List<Avance> avances() { return service.avances(); }
  @PostMapping("/api/v1/avances") public Avance avance(@RequestBody Map<String,String> body) {
    return service.createAvance(Long.valueOf(body.get("tailleurId")), new BigDecimal(body.get("montant")), body.get("motif"));
  }
  @PostMapping("/api/v1/paies/calculer") public Paie calculer(@RequestBody Map<String,String> body) {
    return service.calculer(Long.valueOf(body.get("tailleurId")), LocalDate.parse(body.get("debut")), LocalDate.parse(body.get("fin")),
      body.get("primes")==null?BigDecimal.ZERO:new BigDecimal(body.get("primes")),
      body.get("retenues")==null?BigDecimal.ZERO:new BigDecimal(body.get("retenues")));
  }
  @PostMapping("/api/v1/paies/{id}/valider") public Paie valider(@PathVariable Long id) { return service.valider(id); }
  @PostMapping("/api/v1/paies/{id}/payer") public Paie payer(@PathVariable Long id) { return service.payer(id); }
}
