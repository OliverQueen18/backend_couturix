package com.couturix.paie;

import com.couturix.caisse.CaisseService;
import com.couturix.commande.CommandeRepository;
import com.couturix.domain.enums.ModeRemuneration;
import com.couturix.domain.enums.PaieStatut;
import com.couturix.production.Production;
import com.couturix.production.ProductionRepository;
import com.couturix.tailleur.Tailleur;
import com.couturix.tailleur.TailleurService;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PaieServiceTest {

    @Mock PaieRepository paieRepository;
    @Mock AvanceRepository avanceRepository;
    @Mock TailleurService tailleurService;
    @Mock ProductionRepository productionRepository;
    @Mock CommandeRepository commandeRepository;
    @Mock CaisseService caisseService;

    PaieService service;

    @BeforeEach
    void init() {
        service = new PaieService(paieRepository, avanceRepository, tailleurService, productionRepository,
                commandeRepository, caisseService);
        when(paieRepository.save(any(Paie.class))).thenAnswer(inv -> inv.getArgument(0));
        when(avanceRepository.findByTailleurIdAndStatut(any(), any())).thenReturn(List.of());
    }

    @Test
    void calculePaieFixe() {
        Tailleur t = tailleur(ModeRemuneration.FIXE);
        t.setSalaireFixe(new BigDecimal("150000"));
        when(tailleurService.require(1L)).thenReturn(t);
        when(commandeRepository.findAll()).thenReturn(List.of());
        stubProductions(List.of());

        Paie paie = service.calculer(1L, LocalDate.now().minusDays(30), LocalDate.now(), BigDecimal.ZERO, BigDecimal.ZERO);
        assertEquals(new BigDecimal("150000"), paie.getMontantBrut());
        assertEquals(PaieStatut.BROUILLON, paie.getStatut());
    }

    @Test
    void calculePaiePiece() {
        Tailleur t = tailleur(ModeRemuneration.PIECE);
        t.setTauxPiece(new BigDecimal("5000"));
        when(tailleurService.require(1L)).thenReturn(t);
        when(commandeRepository.findAll()).thenReturn(List.of());
        stubProductions(List.of(new Production(), new Production()));

        Paie paie = service.calculer(1L, LocalDate.now().minusDays(30), LocalDate.now(), BigDecimal.ZERO, BigDecimal.ZERO);
        assertEquals(new BigDecimal("10000"), paie.getMontantBrut());
    }

    private void stubProductions(List<Production> productions) {
        when(productionRepository.findValidatedInPeriod(eq(1L), any(Instant.class), any(Instant.class)))
                .thenReturn(productions);
    }

    private static Tailleur tailleur(ModeRemuneration mode) {
        Tailleur t = new Tailleur();
        t.setId(1L);
        t.setNom("Diop");
        t.setPrenom("Amadou");
        t.setModeRemuneration(mode);
        t.setSalaireFixe(BigDecimal.ZERO);
        t.setTauxPiece(BigDecimal.ZERO);
        t.setPourcentage(BigDecimal.ZERO);
        return t;
    }
}
