package com.couturix.commande;

import com.couturix.commande.dto.AssignerRequest;
import com.couturix.commande.dto.CommandeDto;
import com.couturix.commande.dto.CommandeRequest;
import com.couturix.commande.dto.CommandeSummaryDto;
import com.couturix.commande.dto.StatutRequest;
import com.couturix.common.api.PageResponse;
import com.couturix.domain.enums.CommandeStatut;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/commandes")
@RequiredArgsConstructor
@Tag(name = "Commandes")
public class CommandeController {

    private final CommandeService commandeService;

    @GetMapping
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','ADMIN','GERANT','RECEPTIONNISTE','TAILLEUR','CLIENT','CAISSIER')")
    public PageResponse<CommandeSummaryDto> list(@RequestParam(required = false) String q,
                                                 @RequestParam(required = false) CommandeStatut statut,
                                                 @RequestParam(required = false) Long clientId,
                                                 Pageable pageable) {
        return PageResponse.of(commandeService.search(q, statut, clientId, pageable));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','ADMIN','GERANT','RECEPTIONNISTE')")
    public CommandeDto create(@Valid @RequestBody CommandeRequest request) {
        return commandeService.create(request);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','ADMIN','GERANT','RECEPTIONNISTE','TAILLEUR','CLIENT','CAISSIER')")
    public CommandeDto get(@PathVariable Long id) {
        return commandeService.get(id);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','ADMIN','GERANT','RECEPTIONNISTE')")
    public CommandeDto update(@PathVariable Long id, @Valid @RequestBody CommandeRequest request) {
        return commandeService.update(id, request);
    }

    @PostMapping("/{id}/statut")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','ADMIN','GERANT','RECEPTIONNISTE','TAILLEUR')")
    public CommandeDto statut(@PathVariable Long id, @Valid @RequestBody StatutRequest request) {
        return commandeService.changeStatut(id, request);
    }

    @PostMapping("/{id}/assigner")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','ADMIN','GERANT')")
    public CommandeDto assigner(@PathVariable Long id, @Valid @RequestBody AssignerRequest request) {
        return commandeService.assigner(id, request);
    }
}
