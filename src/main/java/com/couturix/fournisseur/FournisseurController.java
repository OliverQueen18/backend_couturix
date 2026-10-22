package com.couturix.fournisseur;

import com.couturix.fournisseur.dto.FournisseurRequest;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/fournisseurs")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('SUPER_ADMIN','ADMIN','GERANT','MAGASINIER','COMPTABLE')")
public class FournisseurController {

    private final FournisseurService service;

    @GetMapping
    public List<Fournisseur> list() {
        return service.list();
    }

    @PostMapping
    public Fournisseur create(@Valid @RequestBody FournisseurRequest request) {
        return service.create(request);
    }

    @PutMapping("/{id}")
    public Fournisseur update(@PathVariable Long id, @Valid @RequestBody FournisseurRequest request) {
        return service.update(id, request);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        service.delete(id);
    }
}
