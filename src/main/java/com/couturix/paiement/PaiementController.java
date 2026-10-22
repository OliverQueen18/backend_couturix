package com.couturix.paiement;
import com.couturix.domain.enums.MoyenPaiement;
import java.math.BigDecimal; import java.util.List; import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
@RestController @RequestMapping("/api/v1/paiements") @RequiredArgsConstructor
@PreAuthorize("hasAnyRole('SUPER_ADMIN','ADMIN','GERANT','CAISSIER','COMPTABLE')")
public class PaiementController {
  private final PaiementService service;
  @GetMapping public List<Paiement> list() { return service.list(); }
  @PostMapping public Paiement create(@RequestBody Map<String, Object> body) {
    Long commandeId = body.get("commandeId")==null?null:Long.valueOf(body.get("commandeId").toString());
    MoyenPaiement moyen = body.get("moyen")==null?MoyenPaiement.ESPECES:MoyenPaiement.valueOf(body.get("moyen").toString());
    return service.enregistrer(Long.valueOf(body.get("clientId").toString()), commandeId, new BigDecimal(body.get("montant").toString()), moyen, (String) body.get("reference"));
  }
}
