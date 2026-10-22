package com.couturix.commande;

import com.couturix.domain.enums.CommandeStatut;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

class CommandeWorkflowTest {

    @Test
    void accepteLeParcoursNominal() {
        assertDoesNotThrow(() -> CommandeWorkflow.assertTransition(CommandeStatut.BROUILLON, CommandeStatut.CONFIRMEE));
        assertDoesNotThrow(() -> CommandeWorkflow.assertTransition(CommandeStatut.PRETE, CommandeStatut.LIVREE));
    }

    @Test
    void refuseLesSauts() {
        assertThrows(RuntimeException.class, () -> CommandeWorkflow.assertTransition(CommandeStatut.BROUILLON, CommandeStatut.LIVREE));
    }
}
