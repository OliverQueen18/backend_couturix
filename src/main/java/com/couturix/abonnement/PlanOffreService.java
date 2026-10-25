package com.couturix.abonnement;

import com.couturix.common.exception.ApiException;
import com.couturix.domain.enums.PlanCode;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class PlanOffreService {

    private final PlanOffreRepository repository;
    private final PlanOffreCatalog catalog;

    @Transactional
    public List<PlanOffreDto> list() {
        ensureDefaults();
        return Arrays.stream(PlanCode.values())
                .filter(PlanCode::offreAtelier)
                .map(this::require)
                .map(this::toDto)
                .toList();
    }

    @Transactional
    public List<PlanOffreDto> listPublic() {
        return list().stream().filter(PlanOffreDto::actif).toList();
    }

    @Transactional
    public PlanOffreDto update(String code, PlanOffreUpdateRequest body) {
        PlanCode plan = parseAtelier(code);
        ensureDefaults();
        PlanOffre row = require(plan);
        apply(row, body);
        return toDto(repository.save(row));
    }

    public EffectivePlan resolve(PlanCode plan, Instant startedAt) {
        return resolve(plan, startedAt, Instant.now());
    }

    EffectivePlan resolve(PlanCode plan, Instant startedAt, Instant now) {
        PlanCode code = plan == null ? PlanCode.GRATUIT : plan;
        if (code == PlanCode.PLATEFORME) {
            return new EffectivePlan(
                    code.name(), code.getLibelle(), null, true, false, 0, null, null);
        }
        PlanOffre offre = repository.findById(code.name()).orElseGet(() -> defaults(code));
        int trialDays = Math.max(0, offre.getTrialDays());
        Instant start = startedAt == null ? now : startedAt;
        Instant fin = trialDays <= 0 ? null : start.plus(Duration.ofDays(trialDays));
        boolean essai = fin != null && now.isBefore(fin);
        Integer restants = null;
        if (trialDays > 0) {
            restants = 0;
            if (essai) {
                long jours = ChronoUnit.DAYS.between(
                        now.atZone(ZoneOffset.UTC).toLocalDate(),
                        fin.atZone(ZoneOffset.UTC).toLocalDate());
                restants = (int) Math.max(1, jours);
            }
        }
        boolean sms = offre.isSms() || essai;
        return new EffectivePlan(
                code.name(),
                offre.getLibelle() == null || offre.getLibelle().isBlank() ? code.getLibelle() : offre.getLibelle(),
                offre.getMaxAteliers(),
                sms,
                essai,
                trialDays,
                restants,
                fin);
    }

    @Transactional
    public void ensureDefaults() {
        for (PlanCode code : PlanCode.values()) {
            if (!code.offreAtelier()) {
                continue;
            }
            PlanOffre row = repository.findById(code.name()).orElse(null);
            if (row == null) {
                repository.save(defaults(code));
                continue;
            }
            if (blank(row.getDescription()) || row.getFeatures() == null || row.getFeatures().isEmpty()) {
                fillCommercial(row, catalog.get(code));
                repository.save(row);
            }
        }
    }

    private void apply(PlanOffre row, PlanOffreUpdateRequest body) {
        row.setLibelle(body.libelle().trim());
        row.setKicker(trimToNull(body.kicker()));
        row.setDescription(trimToNull(body.description()));
        row.setCta(trimToNull(body.cta()));
        row.setPrixMensuel(body.prixMensuel());
        row.setRemiseAnnuelle(body.remiseAnnuelle());
        row.setTrialDays(body.trialDays());
        row.setMaxAteliers(body.maxAteliers());
        row.setSms(body.sms());
        row.setPopular(body.popular());
        row.setActif(body.actif());
        row.setFeatures(clean(body.features()));
        row.setServices(cleanGroups(body.services()));
        row.setUpdatedAt(Instant.now());
    }

    private void fillCommercial(PlanOffre row, PlanOffreDto seed) {
        if (seed == null) {
            return;
        }
        if (blank(row.getLibelle())) {
            row.setLibelle(seed.libelle());
        }
        if (blank(row.getKicker())) {
            row.setKicker(seed.kicker());
        }
        if (blank(row.getDescription())) {
            row.setDescription(seed.description());
        }
        if (blank(row.getCta())) {
            row.setCta(seed.cta());
        }
        if (row.getPrixMensuel() == 0 && seed.prixMensuel() > 0) {
            row.setPrixMensuel(seed.prixMensuel());
        }
        if (row.getRemiseAnnuelle() <= 0) {
            row.setRemiseAnnuelle(seed.remiseAnnuelle());
        }
        row.setPopular(seed.popular());
        if (row.getFeatures() == null || row.getFeatures().isEmpty()) {
            row.setFeatures(clean(seed.features()));
        }
        if (row.getServices() == null || row.getServices().isEmpty()) {
            row.setServices(cleanGroups(seed.services()));
        }
    }

    private PlanOffre require(PlanCode plan) {
        return repository.findById(plan.name()).orElseGet(() -> repository.save(defaults(plan)));
    }

    private PlanCode parseAtelier(String raw) {
        try {
            PlanCode plan = PlanCode.from(raw);
            if (!plan.offreAtelier()) {
                throw new IllegalArgumentException("not an atelier plan");
            }
            return plan;
        } catch (IllegalArgumentException ex) {
            throw ApiException.badRequest("Offre inconnue");
        }
    }

    private PlanOffre defaults(PlanCode code) {
        PlanOffre row = new PlanOffre();
        row.setCode(code.name());
        row.setUpdatedAt(Instant.now());
        PlanOffreDto seed = catalog.get(code);
        if (seed != null) {
            row.setLibelle(seed.libelle());
            row.setTrialDays(seed.trialDays());
            row.setMaxAteliers(seed.maxAteliers());
            row.setSms(seed.sms());
            row.setPrixMensuel(seed.prixMensuel());
            row.setRemiseAnnuelle(seed.remiseAnnuelle());
            row.setKicker(seed.kicker());
            row.setDescription(seed.description());
            row.setCta(seed.cta());
            row.setPopular(seed.popular());
            row.setActif(seed.actif());
            row.setFeatures(clean(seed.features()));
            row.setServices(cleanGroups(seed.services()));
            return row;
        }
        row.setLibelle(code.getLibelle());
        row.setTrialDays(code == PlanCode.GRATUIT ? 15 : 0);
        row.setMaxAteliers(code.getMaxAteliers());
        row.setSms(code.autoriseSms());
        row.setActif(true);
        row.setRemiseAnnuelle(20);
        return row;
    }

    private PlanOffreDto toDto(PlanOffre row) {
        return new PlanOffreDto(
                row.getCode(),
                row.getLibelle(),
                row.getKicker(),
                row.getDescription(),
                row.getCta(),
                row.getTrialDays(),
                row.getMaxAteliers(),
                row.isSms(),
                row.getPrixMensuel(),
                row.getRemiseAnnuelle(),
                row.isPopular(),
                row.isActif(),
                row.getFeatures() == null ? List.of() : List.copyOf(row.getFeatures()),
                row.getServices() == null ? List.of() : row.getServices());
    }

    private static List<String> clean(List<String> values) {
        if (values == null) {
            return new ArrayList<>();
        }
        return values.stream()
                .map(value -> value == null ? "" : value.trim())
                .filter(value -> !value.isBlank())
                .collect(java.util.stream.Collectors.toCollection(ArrayList::new));
    }

    private static List<PlanServiceGroup> cleanGroups(List<PlanServiceGroup> groups) {
        if (groups == null) {
            return new ArrayList<>();
        }
        List<PlanServiceGroup> out = new ArrayList<>();
        for (PlanServiceGroup group : groups) {
            if (group == null || blank(group.getTitle())) {
                continue;
            }
            PlanServiceGroup copy = new PlanServiceGroup();
            copy.setTitle(group.getTitle().trim());
            copy.setItems(clean(group.getItems()));
            out.add(copy);
        }
        return out;
    }

    private static boolean blank(String value) {
        return value == null || value.isBlank();
    }

    private static String trimToNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}
