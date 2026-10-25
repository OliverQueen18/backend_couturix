package com.couturix.abonnement;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/public/offres")
public class PublicOffreController {

    private final PlanOffreService planOffreService;

    @GetMapping
    public List<PlanOffreDto> list() {
        return planOffreService.listPublic();
    }
}
