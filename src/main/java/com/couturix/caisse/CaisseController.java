package com.couturix.caisse;
import com.couturix.domain.enums.*;
import java.math.BigDecimal; import java.util.List; import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
@RestController @RequestMapping("/api/v1/caisse") @RequiredArgsConstructor
@PreAuthorize("hasAnyRole('SUPER_ADMIN','ADMIN','GERANT','CAISSIER','COMPTABLE')")
public class CaisseController {
  private final CaisseService service; private final CaisseRepository repo;
  @GetMapping public List<Caisse> list() { return repo.findAll(); }
  @GetMapping("/courante") public Caisse courante() { return service.courante(); }
  @PostMapping("/ouvrir") public Caisse ouvrir(@RequestBody Map<String, BigDecimal> body) { return service.ouvrir(body.get("soldeInitial")); }
  @PostMapping("/{id}/operations") public CaisseOperation op(@PathVariable Long id, @RequestBody CaisseOperation body) {
    return service.operation(body.getTypeOperation(), body.getCategorie(), body.getMontant(), body.getMoyenPaiement(), body.getMotif(), body.getReferenceMetier());
  }
  @PostMapping("/{id}/cloturer") public Caisse cloturer(@PathVariable Long id, @RequestBody Map<String, String> body) {
    return service.cloturer(id, new BigDecimal(body.get("soldeReel")), body.get("commentaires"));
  }
  @GetMapping("/rapports")
  public Map<String, Object> rapport(@RequestParam(required=false) String from, @RequestParam(required=false) String to) {
    var range = com.couturix.common.time.QueryInstants.parse(from, to);
    return service.rapport(range.start(), range.endExclusive());
  }
}
