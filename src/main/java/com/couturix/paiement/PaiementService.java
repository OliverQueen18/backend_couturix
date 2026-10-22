package com.couturix.paiement;
import com.couturix.caisse.CaisseService;
import com.couturix.client.Client; import com.couturix.client.ClientService;
import com.couturix.commande.CommandeService;
import com.couturix.common.exception.ApiException;
import com.couturix.domain.enums.*;
import com.couturix.notification.NotificationService;
import java.math.BigDecimal; import java.time.LocalDate; import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service; import org.springframework.transaction.annotation.Transactional;
@Service @RequiredArgsConstructor
public class PaiementService {
  private final PaiementRepository repo;
  private final ClientService clientService;
  private final CommandeService commandeService;
  private final CaisseService caisseService;
  private final NotificationService notificationService;

  public List<Paiement> list() { return repo.findAll(); }

  @Transactional @PreAuthorize("hasAnyRole('SUPER_ADMIN','ADMIN','GERANT','CAISSIER')")
  public Paiement enregistrer(Long clientId, Long commandeId, BigDecimal montant, MoyenPaiement moyen, String reference, LocalDate datePaiement, String notes) {
    if (clientId == null) throw ApiException.badRequest("Client obligatoire");
    if (montant == null) throw ApiException.badRequest("Montant obligatoire");
    Client client = clientService.require(clientId);
    Paiement p = new Paiement(); p.setClient(client); p.setMontant(montant); p.setMoyen(moyen==null?MoyenPaiement.ESPECES:moyen); p.setReference(reference);
    p.setDatePaiement(datePaiement == null ? LocalDate.now() : datePaiement);
    p.setNotes(notes);
    if (commandeId!=null) p.setCommande(commandeService.find(commandeId));
    client.setSolde(client.getSolde().subtract(montant));
    Paiement saved = repo.save(p);
    if (caisseService.courante()!=null) {
      caisseService.operation(TypeOperationCaisse.RECETTE, CategorieOperationCaisse.PAIEMENT_CLIENT, montant, moyen, "Paiement client", saved.getId().toString());
    }
    if (p.getCommande()!=null) notificationService.notifyEvent(EvenementNotification.PAIEMENT, p.getCommande());
    return saved;
  }
}
