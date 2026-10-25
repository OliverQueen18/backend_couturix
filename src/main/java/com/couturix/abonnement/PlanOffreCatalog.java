package com.couturix.abonnement;

import com.couturix.domain.enums.PlanCode;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

@Component
public class PlanOffreCatalog {

    private final Map<String, PlanOffreDto> byCode;

    public PlanOffreCatalog(ObjectMapper objectMapper) {
        try (InputStream in = new ClassPathResource("plan-offres-defaults.json").getInputStream()) {
            List<PlanOffreDto> rows = objectMapper.readValue(in, new TypeReference<>() {});
            this.byCode = rows.stream().collect(Collectors.toMap(PlanOffreDto::code, Function.identity()));
        } catch (IOException ex) {
            throw new IllegalStateException("Impossible de charger les offres par défaut", ex);
        }
    }

    public PlanOffreDto get(PlanCode code) {
        return byCode.get(code.name());
    }
}
