package com.couturix.paiement;

import com.couturix.common.api.JsonMaps;
import com.couturix.domain.enums.MoyenPaiement;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/paiements")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('SUPER_ADMIN','ADMIN','GERANT','CAISSIER','COMPTABLE')")
public class PaiementController {

    private final PaiementService service;

    @GetMapping
    public List<Paiement> list() {
        return service.list();
    }

    @PostMapping
    public Paiement create(@RequestBody Map<String, Object> body) {
        MoyenPaiement moyen = JsonMaps.str(body, "moyen") == null
                ? MoyenPaiement.ESPECES
                : MoyenPaiement.valueOf(JsonMaps.str(body, "moyen"));
        return service.enregistrer(
                JsonMaps.lng(body, "clientId"),
                JsonMaps.lng(body, "commandeId"),
                JsonMaps.decimal(body, "montant"),
                moyen,
                JsonMaps.str(body, "reference"),
                JsonMaps.date(body, "datePaiement"),
                JsonMaps.str(body, "notes"));
    }
}
