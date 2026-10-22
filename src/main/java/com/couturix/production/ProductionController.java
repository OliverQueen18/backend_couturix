package com.couturix.production;

import com.couturix.common.api.PageResponse;
import com.couturix.domain.enums.ProductionStatut;
import com.couturix.production.dto.ProductionDto;
import com.couturix.production.dto.ProductionEtapeRequest;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@Tag(name = "Production")
public class ProductionController {

    private final ProductionService productionService;

    @GetMapping("/api/v1/productions")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','ADMIN','GERANT','TAILLEUR')")
    public PageResponse<ProductionDto> list(@RequestParam(required = false) Long tailleurId,
                                            @RequestParam(required = false) ProductionStatut statut,
                                            Pageable pageable) {
        return PageResponse.of(productionService.search(tailleurId, statut, pageable));
    }

    @PostMapping("/api/v1/productions/{id}/etapes")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','ADMIN','GERANT','TAILLEUR')")
    public ProductionDto addEtape(@PathVariable Long id, @Valid @RequestBody ProductionEtapeRequest request) {
        return productionService.addEtape(id, request);
    }
}
