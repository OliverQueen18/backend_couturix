package com.couturix.atelier;

import com.couturix.atelier.dto.AtelierAvisDto;
import com.couturix.atelier.dto.AvisRequest;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/avis")
@RequiredArgsConstructor
@Tag(name = "Avis ateliers")
@PreAuthorize("hasRole('CLIENT')")
public class AvisController {

    private final AtelierAvisService avisService;

    @GetMapping("/ateliers/{slug}")
    public AtelierAvisDto mine(@PathVariable String slug) {
        return avisService.mine(slug);
    }

    @PutMapping("/ateliers/{slug}")
    public AtelierAvisDto upsert(@PathVariable String slug, @Valid @RequestBody AvisRequest request) {
        return avisService.upsert(slug, request);
    }
}
