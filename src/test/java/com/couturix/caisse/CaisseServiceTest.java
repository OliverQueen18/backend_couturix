package com.couturix.caisse;

import com.couturix.common.exception.ApiException;
import com.couturix.domain.enums.CaisseStatut;
import com.couturix.domain.enums.MoyenPaiement;
import com.couturix.domain.enums.TypeOperationCaisse;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CaisseServiceTest {

    @Mock CaisseRepository caisseRepository;
    @Mock CaisseOperationRepository operationRepository;
    @Mock ClotureCaisseRepository clotureRepository;

    CaisseService service;

    @BeforeEach
    void init() {
        service = new CaisseService(caisseRepository, operationRepository, clotureRepository);
    }

    @Test
    void accepteCategorieLibre() {
        Caisse caisse = new Caisse();
        caisse.setStatut(CaisseStatut.OUVERTE);
        when(caisseRepository.findFirstByStatut(CaisseStatut.OUVERTE)).thenReturn(Optional.of(caisse));
        when(operationRepository.save(any(CaisseOperation.class))).thenAnswer(inv -> inv.getArgument(0));

        CaisseOperation op = service.operation(
                TypeOperationCaisse.DEPENSE, "Achat bazin", BigDecimal.TEN, MoyenPaiement.ESPECES, "Tissu", null);

        assertEquals("Achat bazin", op.getCategorie());
        assertEquals(TypeOperationCaisse.DEPENSE, op.getTypeOperation());
    }

    @Test
    void normaliseCategorieConnue() {
        assertEquals("PAIEMENT_CLIENT", service.normalizeCategorie("paiement client"));
        assertEquals("ACHAT", service.normalizeCategorie("Achat"));
        assertEquals("AUTRE", service.normalizeCategorie("  "));
    }

    @Test
    void refuseCategorieTropLongue() {
        assertThrows(ApiException.class, () -> service.normalizeCategorie("x".repeat(81)));
    }

    @Test
    void categoriesIncluentDefautsEtPersonnalisees() {
        when(operationRepository.findDistinctCategories()).thenReturn(List.of("Achat bazin", "ACHAT"));
        List<String> cats = service.categoriesOperation();
        assertTrue(cats.contains("PAIEMENT_CLIENT"));
        assertTrue(cats.contains("ACHAT"));
        assertTrue(cats.contains("Achat bazin"));
    }
}
