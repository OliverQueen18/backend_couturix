package com.couturix.commande;

import com.couturix.client.Client;
import com.couturix.client.ClientMesure;
import com.couturix.client.ClientMesureRepository;
import com.couturix.client.ClientRepository;
import com.couturix.client.dto.MesureDto;
import com.couturix.client.dto.MesureRequest;
import com.couturix.common.audit.AuditService;
import com.couturix.common.exception.ApiException;
import com.couturix.composition.Composition;
import com.couturix.composition.CompositionRepository;
import com.couturix.composition.CompositionService;
import com.couturix.commande.dto.AssignerRequest;
import com.couturix.commande.dto.CommandeDetailDto;
import com.couturix.commande.dto.CommandeDto;
import com.couturix.commande.dto.CommandeRequest;
import com.couturix.commande.dto.CommandeSummaryDto;
import com.couturix.commande.dto.StatutRequest;
import com.couturix.devis.Devis;
import com.couturix.devis.DevisRepository;
import com.couturix.domain.enums.CommandeStatut;
import com.couturix.domain.enums.EvenementNotification;
import com.couturix.domain.enums.Priorite;
import com.couturix.modele.Modele;
import com.couturix.modele.ModeleRepository;
import com.couturix.modele.dto.DemandeRequest;
import com.couturix.notification.NotificationContext;
import com.couturix.notification.NotificationService;
import com.couturix.production.ProductionService;
import com.couturix.security.SecurityUtils;
import com.couturix.tailleur.Tailleur;
import com.couturix.tailleur.TailleurService;
import com.couturix.user.User;
import com.couturix.user.UserRepository;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.Year;
import java.util.List;
import java.util.Locale;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CommandeService {

    private final CommandeRepository commandeRepository;
    private final ClientRepository clientRepository;
    private final ClientMesureRepository clientMesureRepository;
    private final ModeleRepository modeleRepository;
    private final CompositionRepository compositionRepository;
    private final CompositionService compositionService;
    private final DevisRepository devisRepository;
    private final TailleurService tailleurService;
    private final ProductionService productionService;
    private final NotificationService notificationService;
    private final UserRepository userRepository;
    private final AuditService auditService;

    @Transactional(readOnly = true)
    public Page<CommandeSummaryDto> search(String q, CommandeStatut statut, Long clientId, Pageable pageable) {
        Specification<Commande> spec = (root, query, cb) -> {
            var predicate = cb.conjunction();
            if (q != null && !q.isBlank()) {
                String like = "%" + q.toLowerCase(Locale.ROOT) + "%";
                predicate = cb.and(predicate, cb.or(
                        cb.like(cb.lower(root.get("numero")), like),
                        cb.like(cb.lower(root.get("client").get("nom")), like),
                        cb.like(cb.lower(root.get("client").get("prenom")), like)
                ));
            }
            if (statut != null) {
                predicate = cb.and(predicate, cb.equal(root.get("statut"), statut));
            }
            Long scopedClient = scopedClientId(clientId);
            if (scopedClient != null) {
                predicate = cb.and(predicate, cb.equal(root.get("client").get("id"), scopedClient));
            }
            if (SecurityUtils.hasRole("TAILLEUR") && !staff()) {
                Long tid = tailleurService.findByCurrentUser();
                predicate = cb.and(predicate, cb.equal(root.get("tailleur").get("id"), tid));
            }
            return predicate;
        };
        return commandeRepository.findAll(spec, pageable).map(this::toSummary);
    }

    @Transactional(readOnly = true)
    public CommandeDto get(Long id) {
        Commande commande = find(id);
        assertCanRead(commande);
        return toDto(commande);
    }

    @Transactional
    public CommandeDto create(CommandeRequest request) {
        Commande commande = new Commande();
        commande.setNumero(nextNumero());
        commande.setStatut(CommandeStatut.BROUILLON);
        apply(commande, request);
        applyDelaiModele(commande);
        commandeRepository.save(commande);
        appendHistorique(commande, null, CommandeStatut.BROUILLON, "Création");
        auditService.log(SecurityUtils.currentUserId(), "CREATE", "COMMANDE", commande.getId(), commande.getNumero());
        notify(commande, EvenementNotification.COMMANDE_CREEE);
        return toDto(commande);
    }

    @Transactional
    public CommandeDto update(Long id, CommandeRequest request) {
        Commande commande = find(id);
        if (commande.getStatut() != CommandeStatut.BROUILLON) {
            throw ApiException.badRequest("Seule une commande brouillon peut être modifiée");
        }
        apply(commande, request);
        commandeRepository.save(commande);
        return toDto(commande);
    }

    @Transactional
    public CommandeDto changeStatut(Long id, StatutRequest request) {
        Commande commande = find(id);
        CommandeStatut ancien = commande.getStatut();
        CommandeWorkflow.assertTransition(ancien, request.statut());
        commande.setStatut(request.statut());
        if (request.statut() == CommandeStatut.LIVREE) {
            commande.setDateLivraison(LocalDate.now());
        }
        commandeRepository.save(commande);
        appendHistorique(commande, ancien, request.statut(), request.commentaire());
        if (request.statut() != CommandeStatut.ANNULEE) {
            productionService.onCommandeStatut(commande, request.commentaire());
        } else if (ancien == CommandeStatut.CONFIRMEE) {
            productionService.onCommandeStatut(commande, request.commentaire());
        }
        if (request.statut() == CommandeStatut.CONFIRMEE) {
            productionService.createIfMissing(commande);
            notify(commande, EvenementNotification.COMMANDE_CONFIRMEE);
        } else if (request.statut() == CommandeStatut.PRETE) {
            notify(commande, EvenementNotification.COMMANDE_PRETE);
        } else if (request.statut() == CommandeStatut.LIVREE) {
            notify(commande, EvenementNotification.LIVRAISON);
        } else {
            notify(commande, EvenementNotification.STATUT_CHANGE);
        }
        auditService.log(SecurityUtils.currentUserOptional().map(u -> u.getId()).orElse(null),
                "STATUT", "COMMANDE", id, ancien + "→" + request.statut());
        return toDto(commande);
    }

    @Transactional
    public CommandeDto assigner(Long id, AssignerRequest request) {
        Commande commande = find(id);
        Tailleur tailleur = tailleurService.find(request.tailleurId());
        commande.setTailleur(tailleur);
        commandeRepository.save(commande);
        productionService.assignTailleur(commande, tailleur);
        auditService.log(SecurityUtils.currentUserId(), "ASSIGNER", "COMMANDE", id, tailleur.nomComplet());
        return toDto(commande);
    }

    @Transactional
    public CommandeSummaryDto createFromPublicDemande(DemandeRequest request) {
        Client client = clientRepository.findFirstByTelephone(request.telephone()).orElseGet(() -> {
            Client c = new Client();
            c.setNom(request.nom());
            c.setPrenom(request.prenom());
            c.setTelephone(request.telephone());
            c.setEmail(request.email());
            return clientRepository.save(c);
        });
        Commande commande = new Commande();
        commande.setNumero(nextNumero());
        commande.setClient(client);
        commande.setStatut(CommandeStatut.BROUILLON);
        commande.setQuantite(request.quantite() == null ? 1 : request.quantite());
        commande.setNotes(request.notes());
        if (request.modeleId() != null) {
            Modele modele = modeleRepository.findById(request.modeleId())
                    .orElseThrow(() -> ApiException.notFound("Modèle introuvable"));
            commande.setModele(modele);
            commande.setPrixUnitaire(modele.getPrixIndicatif() == null ? BigDecimal.ZERO : modele.getPrixIndicatif());
            compositionRepository.findFirstByModeleIdAndActiveTrue(modele.getId()).ifPresent(commande::setComposition);
            applyDelaiModele(commande);
        }
        commande.recalculerMontant();
        commandeRepository.save(commande);
        appendHistorique(commande, null, CommandeStatut.BROUILLON, "Demande publique");
        notify(commande, EvenementNotification.COMMANDE_CREEE);
        return toSummary(commande);
    }

    @Transactional
    public Commande createFromDevis(Devis devis) {
        Commande commande = new Commande();
        commande.setNumero(nextNumero());
        commande.setClient(devis.getClient());
        commande.setModele(devis.getModele());
        commande.setComposition(devis.getComposition());
        commande.setDevis(devis);
        commande.setStatut(CommandeStatut.CONFIRMEE);
        commande.setQuantite(devis.getQuantite());
        commande.setPrixUnitaire(devis.getPrixUnitaire());
        commande.setRemise(devis.getRemise());
        commande.recalculerMontant();
        if (devis.getDelaiJours() != null && devis.getDelaiJours() > 0) {
            commande.setDatePrevue(LocalDate.now().plusDays(devis.getDelaiJours()));
        } else {
            applyDelaiModele(commande);
        }
        commandeRepository.save(commande);
        appendHistorique(commande, null, CommandeStatut.CONFIRMEE, "Créée depuis devis " + devis.getNumero());
        productionService.createIfMissing(commande);
        notify(commande, EvenementNotification.COMMANDE_CONFIRMEE);
        return commande;
    }

    public Commande find(Long id) {
        return commandeRepository.findById(id).orElseThrow(() -> ApiException.notFound("Commande introuvable"));
    }

    public Commande require(Long id) {
        return find(id);
    }

    @Transactional
    public CommandeSummaryDto createFromPublic(Long clientId, Long modeleId, String notes) {
        Client client = clientRepository.findById(clientId).orElseThrow(() -> ApiException.notFound("Client introuvable"));
        return createFromPublicDemande(new DemandeRequest(client.getNom(), client.getPrenom(), client.getTelephone(),
                client.getEmail(), modeleId, 1, notes));
    }

    @Transactional
    public Commande createFromDevis(Long clientId, Long modeleId, Long compositionId, int quantite,
                                    BigDecimal prixUnitaire, BigDecimal remise, Long devisId) {
        Devis devis = devisRepository.findById(devisId).orElseThrow(() -> ApiException.notFound("Devis introuvable"));
        return createFromDevis(devis);
    }

    public CommandeSummaryDto toSummary(Commande c) {
        return new CommandeSummaryDto(
                c.getId(), c.getNumero(),
                c.getClient().getId(), c.getClient().nomComplet(),
                c.getModele() == null ? null : c.getModele().getId(),
                c.getModele() == null ? null : c.getModele().getNom(),
                c.getTailleur() == null ? null : c.getTailleur().getId(),
                c.getStatut(), c.getPriorite(), c.getDatePrevue(), c.getDateLivraison(),
                c.getQuantite(), c.getMontantTotal(), c.isRetard(), c.getCreatedAt()
        );
    }

    public CommandeDto toDto(Commande c) {
        List<CommandeDetailDto> details = c.getDetails().stream()
                .map(d -> new CommandeDetailDto(d.getId(), d.getDesignation(), d.getQuantite(), d.getPrixUnitaire()))
                .toList();
        return new CommandeDto(
                c.getId(), c.getNumero(),
                c.getClient().getId(), c.getClient().nomComplet(),
                c.getModele() == null ? null : c.getModele().getId(),
                c.getModele() == null ? null : c.getModele().getNom(),
                c.getComposition() == null ? null : c.getComposition().getId(),
                c.getDevis() == null ? null : c.getDevis().getId(),
                c.getTailleur() == null ? null : c.getTailleur().getId(),
                c.getTailleur() == null ? null : c.getTailleur().nomComplet(),
                c.getStatut(), c.getPriorite(), c.getDatePrevue(), c.getDateLivraison(),
                c.getQuantite(), c.getPrixUnitaire(), c.getRemise(), c.getMontantTotal(),
                c.getNotes(), c.isRetard(), details, toMesureDto(c.getMesure()), c.getCreatedAt()
        );
    }

    synchronized String nextNumero() {
        String prefix = "CMD-" + Year.now().getValue() + "-";
        int next = commandeRepository.findMaxNumero(prefix)
                .map(n -> Integer.parseInt(n.substring(prefix.length())) + 1)
                .orElse(1);
        return prefix + String.format("%04d", next);
    }

    private void apply(Commande commande, CommandeRequest request) {
        Client client = clientRepository.findById(request.clientId())
                .orElseThrow(() -> ApiException.notFound("Client introuvable"));
        commande.setClient(client);
        if (request.modeleId() != null) {
            commande.setModele(modeleRepository.findById(request.modeleId())
                    .orElseThrow(() -> ApiException.notFound("Modèle introuvable")));
        } else {
            commande.setModele(null);
        }
        if (request.compositionId() != null) {
            Composition composition = compositionService.find(request.compositionId());
            commande.setComposition(composition);
        }
        if (request.devisId() != null) {
            commande.setDevis(devisRepository.findById(request.devisId())
                    .orElseThrow(() -> ApiException.notFound("Devis introuvable")));
        }
        if (request.tailleurId() != null) {
            commande.setTailleur(tailleurService.find(request.tailleurId()));
        }
        commande.setPriorite(request.priorite() == null ? Priorite.NORMALE : request.priorite());
        commande.setDatePrevue(request.datePrevue());
        commande.setQuantite(request.quantite() == null ? 1 : request.quantite());
        BigDecimal pu = request.prixUnitaire();
        if (pu == null && commande.getModele() != null && commande.getModele().getPrixIndicatif() != null) {
            pu = commande.getModele().getPrixIndicatif();
        }
        commande.setPrixUnitaire(pu == null ? BigDecimal.ZERO : pu);
        commande.setRemise(request.remise() == null ? BigDecimal.ZERO : request.remise());
        commande.setNotes(request.notes());
        commande.getDetails().clear();
        if (request.details() != null) {
            for (CommandeDetailDto d : request.details()) {
                CommandeDetail detail = new CommandeDetail();
                detail.setCommande(commande);
                detail.setDesignation(d.designation());
                detail.setQuantite(d.quantite());
                detail.setPrixUnitaire(d.prixUnitaire() == null ? BigDecimal.ZERO : d.prixUnitaire());
                commande.getDetails().add(detail);
            }
        }
        if (request.mesure() != null) {
            CommandeMesure mesure = commande.getMesure() == null ? new CommandeMesure() : commande.getMesure();
            copyMesure(mesure, request.mesure());
            mesure.setCommande(commande);
            commande.setMesure(mesure);
        } else if (commande.getMesure() == null) {
            clientMesureRepository.findFirstByClientIdAndCouranteTrue(client.getId()).ifPresent(cm -> {
                CommandeMesure mesure = copyFromClient(cm);
                mesure.setCommande(commande);
                commande.setMesure(mesure);
            });
        }
        commande.recalculerMontant();
    }

    private void applyDelaiModele(Commande commande) {
        if (commande.getDatePrevue() != null) {
            return;
        }
        Modele modele = commande.getModele();
        if (modele != null && modele.getTempsFabricationJours() != null && modele.getTempsFabricationJours() > 0) {
            commande.setDatePrevue(LocalDate.now().plusDays(modele.getTempsFabricationJours()));
        }
    }

    private void appendHistorique(Commande commande, CommandeStatut ancien, CommandeStatut nouveau, String commentaire) {
        CommandeStatutHistorique h = new CommandeStatutHistorique();
        h.setCommande(commande);
        h.setAncien(ancien == null ? null : ancien.name());
        h.setNouveau(nouveau.name());
        h.setCommentaire(commentaire);
        SecurityUtils.currentUserOptional().ifPresent(p ->
                h.setUser(userRepository.findById(p.getId()).orElse(null)));
        commande.getHistorique().add(h);
    }

    private void notify(Commande commande, EvenementNotification event) {
        Client client = commande.getClient();
        notificationService.notifyEvent(event, new NotificationContext(
                commande.getNumero(),
                client.nomComplet(),
                commande.getStatut().name(),
                commande.getMontantTotal() == null ? "0" : commande.getMontantTotal().toPlainString(),
                null,
                client.getEmail(),
                client.getTelephone()
        ));
    }

    private void assertCanRead(Commande commande) {
        if (SecurityUtils.hasRole("CLIENT") && !staff()) {
            Client mine = clientRepository.findByUserId(SecurityUtils.currentUserId()).orElse(null);
            if (mine == null || !mine.getId().equals(commande.getClient().getId())) {
                throw ApiException.forbidden("Commande non autorisée");
            }
        }
    }

    private Long scopedClientId(Long clientId) {
        if (SecurityUtils.hasRole("CLIENT") && !staff()) {
            return clientRepository.findByUserId(SecurityUtils.currentUserId()).map(Client::getId).orElse(-1L);
        }
        return clientId;
    }

    private boolean staff() {
        return SecurityUtils.hasRole("SUPER_ADMIN") || SecurityUtils.hasRole("ADMIN") || SecurityUtils.hasRole("GERANT")
                || SecurityUtils.hasRole("RECEPTIONNISTE") || SecurityUtils.hasRole("CAISSIER");
    }

    private static MesureDto toMesureDto(CommandeMesure m) {
        if (m == null) {
            return null;
        }
        return new MesureDto(m.getId(), m.getTourCou(), m.getTourPoitrine(), m.getTourTaille(), m.getTourHanches(),
                m.getTourBras(), m.getTourPoignet(), m.getEpaule(), m.getLongueurDos(), m.getLongueurDevant(),
                m.getLongueurManche(), m.getTourCuisse(), m.getTourGenou(), m.getBasPantalon(), m.getLongueurPantalon(),
                m.getHauteur(), m.getNotes(), null, true, null);
    }

    private static void copyMesure(CommandeMesure m, MesureRequest r) {
        m.setTourCou(r.tourCou());
        m.setTourPoitrine(r.tourPoitrine());
        m.setTourTaille(r.tourTaille());
        m.setTourHanches(r.tourHanches());
        m.setTourBras(r.tourBras());
        m.setTourPoignet(r.tourPoignet());
        m.setEpaule(r.epaule());
        m.setLongueurDos(r.longueurDos());
        m.setLongueurDevant(r.longueurDevant());
        m.setLongueurManche(r.longueurManche());
        m.setTourCuisse(r.tourCuisse());
        m.setTourGenou(r.tourGenou());
        m.setBasPantalon(r.basPantalon());
        m.setLongueurPantalon(r.longueurPantalon());
        m.setHauteur(r.hauteur());
        m.setNotes(r.notes());
    }

    private static CommandeMesure copyFromClient(ClientMesure cm) {
        CommandeMesure m = new CommandeMesure();
        m.setTourCou(cm.getTourCou());
        m.setTourPoitrine(cm.getTourPoitrine());
        m.setTourTaille(cm.getTourTaille());
        m.setTourHanches(cm.getTourHanches());
        m.setTourBras(cm.getTourBras());
        m.setTourPoignet(cm.getTourPoignet());
        m.setEpaule(cm.getEpaule());
        m.setLongueurDos(cm.getLongueurDos());
        m.setLongueurDevant(cm.getLongueurDevant());
        m.setLongueurManche(cm.getLongueurManche());
        m.setTourCuisse(cm.getTourCuisse());
        m.setTourGenou(cm.getTourGenou());
        m.setBasPantalon(cm.getBasPantalon());
        m.setLongueurPantalon(cm.getLongueurPantalon());
        m.setHauteur(cm.getHauteur());
        m.setNotes(cm.getNotes());
        return m;
    }
}
