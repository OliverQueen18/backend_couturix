package com.couturix.abonnement;

import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/admin/offres")
@PreAuthorize("hasAnyRole('SUPER_ADMIN','ADMIN')")
public class PlanOffreController {

    private final PlanOffreService planOffreService;

    @GetMapping
    public List<PlanOffreDto> list() {
        return planOffreService.list();
    }

    @PutMapping("/{code}")
    public PlanOffreDto update(@PathVariable String code, @Valid @RequestBody PlanOffreUpdateRequest body) {
        return planOffreService.update(code, body);
    }
}
