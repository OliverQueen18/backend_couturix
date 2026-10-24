package com.couturix.caisse;

import com.couturix.common.api.JsonMaps;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/caisse")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('SUPER_ADMIN','ADMIN','GERANT','CAISSIER','COMPTABLE')")
public class CaisseController {

    private final CaisseService service;
    private final CaisseRepository repo;

    @GetMapping
    public List<Caisse> list() {
        return repo.findAll();
    }

    @GetMapping("/courante")
    public Caisse courante() {
        return service.courante();
    }

    @GetMapping("/categories-operation")
    public List<String> categories() {
        return service.categoriesOperation();
    }

    @PostMapping("/ouvrir")
    public Caisse ouvrir(@RequestBody Map<String, Object> body) {
        return service.ouvrir(JsonMaps.decimal(body, "soldeInitial"));
    }

    @PostMapping("/{id}/operations")
    public CaisseOperation op(@PathVariable Long id, @RequestBody CaisseOperation body) {
        return service.operation(
                body.getTypeOperation(),
                body.getCategorie(),
                body.getMontant(),
                body.getMoyenPaiement(),
                body.getMotif(),
                body.getReferenceMetier());
    }

    @PostMapping("/{id}/cloturer")
    public Caisse cloturer(@PathVariable Long id, @RequestBody Map<String, Object> body) {
        return service.cloturer(id, JsonMaps.decimal(body, "soldeReel"), JsonMaps.str(body, "commentaires"));
    }

    @GetMapping("/rapports")
    public Map<String, Object> rapport(@RequestParam(required = false) String from, @RequestParam(required = false) String to) {
        var range = com.couturix.common.time.QueryInstants.parse(from, to);
        return service.rapport(range.start(), range.endExclusive());
    }
}
