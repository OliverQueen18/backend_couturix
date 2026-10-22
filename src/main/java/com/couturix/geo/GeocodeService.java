package com.couturix.geo;

import java.text.Normalizer;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.util.UriComponentsBuilder;

@Slf4j
@Service
public class GeocodeService {

    private static final String NOMINATIM = "https://nominatim.openstreetmap.org/search";
    private static final Set<String> NOISE = Set.of(
            "rue", "avenue", "av", "boulevard", "bd", "impasse", "cite", "quartier", "bp", "lot");
    private static final Map<String, String> COUNTRY_CODES = countryCodes();

    private final Object gate = new Object();
    private long nextCallAt;
    private final RestClient restClient = RestClient.builder()
            .defaultHeader(HttpHeaders.USER_AGENT, "CouturiX/1.0 (noreply@couturix.local)")
            .defaultHeader(HttpHeaders.ACCEPT, "application/json")
            .build();

    public Optional<GeoPoint> lookup(String query) {
        return lookupParts(query, null, null);
    }

    public Optional<GeoPoint> lookupParts(String adresse, String ville, String pays) {
        String street = clean(adresse);
        String city = clean(ville);
        String country = clean(pays);
        String code = countryCode(country);
        List<Candidate> candidates = new ArrayList<>();
        if (!street.isBlank()) {
            candidates.add(new Candidate(street, code, "street"));
            String token = mainToken(street);
            if (!token.isBlank() && !token.equalsIgnoreCase(street)) {
                candidates.add(new Candidate(token, code, "street"));
            }
            if (token.contains("-")) {
                candidates.add(new Candidate(token.replace('-', ' '), code, "street"));
            }
        }
        if (!city.isBlank()) {
            candidates.add(new Candidate(city, code, "city"));
        }
        if (candidates.size() > 3) {
            candidates = new ArrayList<>(candidates.subList(0, 3));
        }
        if (!country.isBlank()) {
            candidates.add(new Candidate(country, null, "country"));
        }
        Map<String, Candidate> unique = new LinkedHashMap<>();
        for (Candidate candidate : candidates) {
            unique.putIfAbsent(fold(candidate.query()) + "|" + candidate.code(), candidate);
        }
        for (Candidate candidate : unique.values()) {
            Optional<GeoPoint> hit = search(candidate.query(), candidate.code());
            if (hit.isPresent()) {
                GeoPoint point = hit.get();
                return Optional.of(new GeoPoint(point.latitude(), point.longitude(), point.label(), candidate.precision()));
            }
        }
        return Optional.empty();
    }

    private Optional<GeoPoint> search(String query, String countryCode) {
        awaitTurn();
        try {
            UriComponentsBuilder builder = UriComponentsBuilder.fromUriString(NOMINATIM)
                    .queryParam("q", query)
                    .queryParam("format", "jsonv2")
                    .queryParam("limit", 1)
                    .queryParam("addressdetails", 0)
                    .queryParam("accept-language", "fr");
            if (countryCode != null && !countryCode.isBlank()) {
                builder.queryParam("countrycodes", countryCode);
            }
            List<Map<String, Object>> results = restClient.get()
                    .uri(builder.encode().build().toUri())
                    .retrieve()
                    .body(new ParameterizedTypeReference<>() {});
            if (results == null || results.isEmpty()) {
                return Optional.empty();
            }
            Map<String, Object> first = results.getFirst();
            double lat = Double.parseDouble(String.valueOf(first.get("lat")));
            double lon = Double.parseDouble(String.valueOf(first.get("lon")));
            String label = String.valueOf(first.getOrDefault("display_name", query));
            return Optional.of(new GeoPoint(lat, lon, label, "street"));
        } catch (Exception ex) {
            log.warn("Géocodage impossible pour '{}': {}", query, ex.getMessage());
            return Optional.empty();
        }
    }

    private void awaitTurn() {
        synchronized (gate) {
            long wait = nextCallAt - System.currentTimeMillis();
            if (wait > 0) {
                try {
                    Thread.sleep(wait);
                } catch (InterruptedException ex) {
                    Thread.currentThread().interrupt();
                }
            }
            nextCallAt = System.currentTimeMillis() + 1100;
        }
    }

    private static String mainToken(String street) {
        String chosen = "";
        for (String part : street.split("\\s+")) {
            String token = part.replaceAll("^[,.]+|[,.]+$", "");
            if (token.length() < 3 || token.chars().allMatch(Character::isDigit)) {
                continue;
            }
            if (NOISE.contains(fold(token))) {
                continue;
            }
            chosen = token;
            break;
        }
        return chosen;
    }

    private static String countryCode(String country) {
        if (country == null || country.isBlank()) {
            return null;
        }
        return COUNTRY_CODES.get(fold(country));
    }

    private static String clean(String value) {
        return value == null ? "" : value.trim().replaceAll("\\s+", " ");
    }

    private static String fold(String value) {
        String normalized = Normalizer.normalize(value, Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "")
                .replace('\u2019', '\'')
                .toLowerCase(Locale.ROOT)
                .trim();
        return normalized;
    }

    private static Map<String, String> countryCodes() {
        Map<String, String> codes = new LinkedHashMap<>();
        for (String iso : Locale.getISOCountries()) {
            String code = iso.toLowerCase(Locale.ROOT);
            Locale locale = Locale.of("", iso);
            codes.put(fold(locale.getDisplayCountry(Locale.FRENCH)), code);
            codes.put(fold(locale.getDisplayCountry(Locale.ENGLISH)), code);
            codes.put(code, code);
        }
        return codes;
    }

    private record Candidate(String query, String code, String precision) {}
}
