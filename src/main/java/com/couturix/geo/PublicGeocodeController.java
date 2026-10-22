package com.couturix.geo;

import com.couturix.common.exception.ApiException;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/public/geocode")
public class PublicGeocodeController {

    private final GeocodeService geocodeService;

    public PublicGeocodeController(GeocodeService geocodeService) {
        this.geocodeService = geocodeService;
    }

    @GetMapping
    public GeoPoint search(
            @RequestParam(value = "q", required = false) String query,
            @RequestParam(value = "adresse", required = false) String adresse,
            @RequestParam(value = "ville", required = false) String ville,
            @RequestParam(value = "pays", required = false) String pays) {
        boolean detailed = notBlank(adresse) || notBlank(ville) || notBlank(pays);
        return (detailed ? geocodeService.lookupParts(adresse, ville, pays) : geocodeService.lookup(query))
                .orElseThrow(() -> ApiException.notFound("Adresse introuvable sur la carte"));
    }

    private static boolean notBlank(String value) {
        return value != null && !value.isBlank();
    }
}
