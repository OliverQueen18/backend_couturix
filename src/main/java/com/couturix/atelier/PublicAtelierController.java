package com.couturix.atelier;

import com.couturix.atelier.dto.AtelierAvisDto;
import com.couturix.atelier.dto.AtelierPublicDto;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/public/ateliers")
@RequiredArgsConstructor
public class PublicAtelierController {

    private final AtelierService atelierService;
    private final AtelierAvisService avisService;

    @GetMapping
    public List<AtelierPublicDto> list(@RequestParam(required = false) String q) {
        return atelierService.publicList(q);
    }

    @GetMapping("/{slug}")
    public AtelierPublicDto get(@PathVariable String slug) {
        return atelierService.publicBySlug(slug);
    }

    @GetMapping("/{slug}/avis")
    public List<AtelierAvisDto> avis(@PathVariable String slug) {
        return avisService.list(slug);
    }
}
