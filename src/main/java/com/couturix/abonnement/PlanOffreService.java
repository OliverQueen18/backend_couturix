package com.couturix.abonnement;

import com.couturix.common.exception.ApiException;
import com.couturix.domain.enums.PlanCode;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class PlanOffreService {

    private final PlanOffreRepository repository;

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
    public PlanOffreDto updateTrialDays(String code, Integer trialDays) {
        PlanCode plan = parseAtelier(code);
        if (trialDays == null || trialDays < 0 || trialDays > 3650) {
            throw ApiException.badRequest("Le délai d’essai doit être entre 0 et 3650 jours");
        }
        ensureDefaults();
        PlanOffre row = require(plan);
        row.setTrialDays(trialDays);
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
            if (repository.existsById(code.name())) {
                continue;
            }
            repository.save(defaults(code));
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
        row.setLibelle(code.getLibelle());
        row.setTrialDays(code == PlanCode.GRATUIT ? 15 : 0);
        row.setMaxAteliers(code.getMaxAteliers());
        row.setSms(code.autoriseSms());
        row.setUpdatedAt(Instant.now());
        return row;
    }

    private PlanOffreDto toDto(PlanOffre row) {
        return new PlanOffreDto(row.getCode(), row.getLibelle(), row.getTrialDays(), row.getMaxAteliers(), row.isSms());
    }
}
