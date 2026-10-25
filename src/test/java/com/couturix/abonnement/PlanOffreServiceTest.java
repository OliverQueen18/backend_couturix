package com.couturix.abonnement;

import com.couturix.domain.enums.PlanCode;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PlanOffreServiceTest {

    @Mock PlanOffreRepository repository;
    @Mock PlanOffreCatalog catalog;

    PlanOffreService service;

    @BeforeEach
    void init() {
        service = new PlanOffreService(repository, catalog);
    }

    @Test
    void gratuitEssaiDebloqueSmsPendant15Jours() {
        Instant start = Instant.parse("2026-10-01T00:00:00Z");
        Instant now = start.plus(5, ChronoUnit.DAYS);
        when(repository.findById("GRATUIT")).thenReturn(Optional.empty());

        EffectivePlan plan = service.resolve(PlanCode.GRATUIT, start, now);

        assertTrue(plan.essai());
        assertTrue(plan.sms());
        assertEquals(15, plan.essaiJours());
        assertEquals(10, plan.essaiJoursRestants());
    }

    @Test
    void gratuitSansSmsApresEssai() {
        Instant start = Instant.parse("2026-10-01T00:00:00Z");
        Instant now = start.plus(16, ChronoUnit.DAYS);
        when(repository.findById("GRATUIT")).thenReturn(Optional.empty());

        EffectivePlan plan = service.resolve(PlanCode.GRATUIT, start, now);

        assertFalse(plan.essai());
        assertFalse(plan.sms());
        assertEquals(0, plan.essaiJoursRestants());
    }

    @Test
    void delaiConfigurableParOffre() {
        PlanOffre row = new PlanOffre();
        row.setCode("GRATUIT");
        row.setLibelle("Gratuit");
        row.setTrialDays(3);
        row.setMaxAteliers(1);
        row.setSms(false);
        when(repository.findById("GRATUIT")).thenReturn(Optional.of(row));

        Instant start = Instant.parse("2026-10-01T00:00:00Z");
        EffectivePlan encore = service.resolve(PlanCode.GRATUIT, start, start.plus(2, ChronoUnit.DAYS));
        EffectivePlan fini = service.resolve(PlanCode.GRATUIT, start, start.plus(4, ChronoUnit.DAYS));

        assertTrue(encore.sms());
        assertEquals(3, encore.essaiJours());
        assertFalse(fini.sms());
    }

    @Test
    void standardGardeSmsSansEssai() {
        when(repository.findById("STANDARD")).thenReturn(Optional.empty());
        EffectivePlan plan = service.resolve(PlanCode.STANDARD, Instant.now(), Instant.now());
        assertTrue(plan.sms());
        assertFalse(plan.essai());
        assertEquals(0, plan.essaiJours());
    }
}
