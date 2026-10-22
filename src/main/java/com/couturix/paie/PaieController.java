package com.couturix.paie;

import com.couturix.common.api.JsonMaps;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('SUPER_ADMIN','ADMIN','GERANT','COMPTABLE')")
public class PaieController {

    private final PaieService service;

    @GetMapping("/api/v1/paies")
    public List<Paie> paies() {
        return service.list();
    }

    @GetMapping("/api/v1/avances")
    public List<Avance> avances() {
        return service.avances();
    }

    @PostMapping("/api/v1/avances")
    public Avance avance(@RequestBody Map<String, Object> body) {
        return service.createAvance(JsonMaps.lng(body, "tailleurId"), JsonMaps.decimal(body, "montant"), JsonMaps.str(body, "motif"));
    }

    @PostMapping("/api/v1/paies/calculer")
    public Paie calculer(@RequestBody Map<String, Object> body) {
        BigDecimal primes = JsonMaps.decimal(body, "primes");
        BigDecimal retenues = JsonMaps.decimal(body, "retenues");
        return service.calculer(
                JsonMaps.lng(body, "tailleurId"),
                JsonMaps.date(body, "periodeDebut", "debut"),
                JsonMaps.date(body, "periodeFin", "fin"),
                primes == null ? BigDecimal.ZERO : primes,
                retenues == null ? BigDecimal.ZERO : retenues);
    }

    @PostMapping("/api/v1/paies/{id}/valider")
    public Paie valider(@PathVariable Long id) {
        return service.valider(id);
    }

    @PostMapping("/api/v1/paies/{id}/payer")
    public Paie payer(@PathVariable Long id) {
        return service.payer(id);
    }
}
