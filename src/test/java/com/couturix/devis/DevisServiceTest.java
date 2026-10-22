package com.couturix.devis;

import com.couturix.client.Client;
import com.couturix.client.ClientService;
import com.couturix.commande.Commande;
import com.couturix.commande.CommandeService;
import com.couturix.composition.CompositionRepository;
import com.couturix.domain.enums.CommandeStatut;
import com.couturix.domain.enums.DevisStatut;
import com.couturix.modele.ModeleService;
import com.couturix.simulateur.SimulateurService;
import java.math.BigDecimal;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DevisServiceTest {

    @Mock DevisRepository repo;
    @Mock ClientService clientService;
    @Mock ModeleService modeleService;
    @Mock CompositionRepository compositionRepository;
    @Mock SimulateurService simulateurService;
    @Mock CommandeService commandeService;

    DevisService service;

    @BeforeEach
    void init() {
        service = new DevisService(repo, clientService, modeleService, compositionRepository, simulateurService, commandeService);
        when(repo.save(any(Devis.class))).thenAnswer(inv -> inv.getArgument(0));
    }

    @Test
    void accepterCreeUneCommandeConfirmee() {
        Client client = new Client();
        client.setId(8L);
        Devis devis = new Devis();
        devis.setId(3L);
        devis.setNumero("DEV-2026-0001");
        devis.setClient(client);
        devis.setQuantite(1);
        devis.setPrixUnitaire(new BigDecimal("25000"));
        devis.setRemise(BigDecimal.ZERO);
        devis.setStatut(DevisStatut.BROUILLON);
        when(repo.findById(3L)).thenReturn(Optional.of(devis));

        Commande commande = new Commande();
        commande.setStatut(CommandeStatut.CONFIRMEE);
        when(commandeService.createFromDevis(any(Devis.class))).thenReturn(commande);

        Devis result = service.accepter(3L);
        assertEquals(DevisStatut.COMMANDE, result.getStatut());
        verify(commandeService).createFromDevis(devis);
    }
}
