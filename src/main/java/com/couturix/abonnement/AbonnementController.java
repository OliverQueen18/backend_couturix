package com.couturix.abonnement;

import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/abonnements")
public class AbonnementController {

    private final AbonnementService abonnementService;

    public AbonnementController(AbonnementService abonnementService) {
        this.abonnementService = abonnementService;
    }

    @GetMapping("/courant")
    public AbonnementDto courant() {
        return abonnementService.courant();
    }

    @PutMapping("/utilisateurs/{id}")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public AbonnementDto assigner(@PathVariable Long id, @Valid @RequestBody PlanAssignRequest request) {
        return abonnementService.assigner(id, request.plan());
    }
}
