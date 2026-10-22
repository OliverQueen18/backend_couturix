package com.couturix.client;

import com.couturix.client.dto.ClientDetailDto;
import com.couturix.client.dto.ClientDto;
import com.couturix.client.dto.ClientRequest;
import com.couturix.client.dto.MesureDto;
import com.couturix.commande.CommandeRepository;
import com.couturix.commande.CommandeService;
import com.couturix.common.api.PageResponse;
import com.couturix.common.audit.AuditService;
import com.couturix.common.exception.ApiException;
import com.couturix.paiement.PaiementRepository;
import com.couturix.paiement.dto.PaiementDto;
import com.couturix.storage.FileStorageService;
import jakarta.persistence.criteria.Predicate;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
public class ClientService {

    private final ClientRepository clientRepository;
    private final ClientMesureRepository mesureRepository;
    private final FileStorageService fileStorageService;
    private final AuditService auditService;
    private final CommandeRepository commandeRepository;
    private final PaiementRepository paiementRepository;
    private final CommandeService commandeService;

    @Transactional(readOnly = true)
    public PageResponse<ClientDto> search(String q, Pageable pageable) {
        Specification<Client> spec = (root, query, cb) -> {
            if (q == null || q.isBlank()) {
                return cb.conjunction();
            }
            String like = "%" + q.toLowerCase() + "%";
            List<Predicate> predicates = new ArrayList<>();
            predicates.add(cb.like(cb.lower(root.get("nom")), like));
            predicates.add(cb.like(cb.lower(root.get("prenom")), like));
            predicates.add(cb.like(cb.lower(root.get("telephone")), like));
            predicates.add(cb.like(cb.lower(cb.coalesce(root.get("email"), "")), like));
            return cb.or(predicates.toArray(Predicate[]::new));
        };
        return PageResponse.of(clientRepository.findAll(spec, pageable).map(this::toDto));
    }

    @Transactional(readOnly = true)
    public ClientDetailDto get(Long id) {
        Client client = require(id);
        MesureDto courante = mesureRepository.findFirstByClientIdAndCouranteTrue(id).map(this::toMesure).orElse(null);
        var commandes = commandeRepository.findTop5ByClientIdOrderByCreatedAtDesc(id).stream()
                .map(commandeService::toSummary).toList();
        var paiements = paiementRepository.findByClientIdOrderByCreatedAtDesc(id).stream()
                .limit(5)
                .map(p -> new PaiementDto(p.getId(), p.getCommande() == null ? null : p.getCommande().getId(),
                        p.getClient().getId(), p.getMontant(), p.getMoyen(), p.getDatePaiement(),
                        p.getReference(), p.getNotes(), p.getCreatedAt()))
                .toList();
        return new ClientDetailDto(toDto(client), courante, commandes, paiements);
    }

    @Transactional
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','ADMIN','GERANT','RECEPTIONNISTE')")
    public ClientDto create(ClientRequest request) {
        Client client = new Client();
        apply(client, request);
        Client saved = clientRepository.save(client);
        auditService.log(null, "CREATE_CLIENT", "CLIENT", saved.getId(), saved.nomComplet());
        return toDto(saved);
    }

    @Transactional
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','ADMIN','GERANT','RECEPTIONNISTE')")
    public ClientDto update(Long id, ClientRequest request) {
        Client client = require(id);
        apply(client, request);
        return toDto(clientRepository.save(client));
    }

    @Transactional
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','ADMIN','GERANT')")
    public void delete(Long id) {
        clientRepository.delete(require(id));
        auditService.log(null, "DELETE_CLIENT", "CLIENT", id, null);
    }

    @Transactional(readOnly = true)
    public List<MesureDto> mesures(Long clientId) {
        require(clientId);
        return mesureRepository.findByClientIdOrderByDatePriseDesc(clientId).stream().map(this::toMesure).toList();
    }

    @Transactional
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','ADMIN','GERANT','RECEPTIONNISTE')")
    public MesureDto addMesure(Long clientId, MesureDto dto) {
        Client client = require(clientId);
        mesureRepository.findFirstByClientIdAndCouranteTrue(clientId).ifPresent(current -> {
            current.setCourante(false);
            mesureRepository.save(current);
        });
        ClientMesure mesure = new ClientMesure();
        mesure.setClient(client);
        copyMesure(dto, mesure);
        mesure.setCourante(true);
        return toMesure(mesureRepository.save(mesure));
    }

    @Transactional
    public ClientDto uploadPhoto(Long id, MultipartFile file) {
        Client client = require(id);
        client.setPhotoUrl(fileStorageService.store(file, "clients"));
        return toDto(clientRepository.save(client));
    }

    public Client require(Long id) {
        return clientRepository.findById(id).orElseThrow(() -> ApiException.notFound("Client introuvable"));
    }

    private void apply(Client client, ClientRequest request) {
        client.setNom(request.nom());
        client.setPrenom(request.prenom());
        client.setTelephone(request.telephone());
        client.setEmail(request.email());
        client.setAdresse(request.adresse());
        client.setNotes(request.notes());
    }

    private void copyMesure(MesureDto dto, ClientMesure m) {
        m.setTourCou(dto.tourCou());
        m.setTourPoitrine(dto.tourPoitrine());
        m.setTourTaille(dto.tourTaille());
        m.setTourHanches(dto.tourHanches());
        m.setTourBras(dto.tourBras());
        m.setTourPoignet(dto.tourPoignet());
        m.setEpaule(dto.epaule());
        m.setLongueurDos(dto.longueurDos());
        m.setLongueurDevant(dto.longueurDevant());
        m.setLongueurManche(dto.longueurManche());
        m.setTourCuisse(dto.tourCuisse());
        m.setTourGenou(dto.tourGenou());
        m.setBasPantalon(dto.basPantalon());
        m.setLongueurPantalon(dto.longueurPantalon());
        m.setHauteur(dto.hauteur());
        m.setNotes(dto.notes());
        if (dto.datePrise() != null) {
            m.setDatePrise(dto.datePrise());
        }
    }

    public ClientDto toDto(Client c) {
        return new ClientDto(c.getId(),
                c.getUser() == null ? null : c.getUser().getId(),
                c.getNom(), c.getPrenom(), c.getTelephone(), c.getEmail(),
                c.getAdresse(), c.getPhotoUrl(), c.getNotes(), c.getSolde(), c.getCreatedAt());
    }

    private MesureDto toMesure(ClientMesure m) {
        return new MesureDto(m.getId(), m.getTourCou(), m.getTourPoitrine(), m.getTourTaille(), m.getTourHanches(),
                m.getTourBras(), m.getTourPoignet(), m.getEpaule(), m.getLongueurDos(), m.getLongueurDevant(),
                m.getLongueurManche(), m.getTourCuisse(), m.getTourGenou(), m.getBasPantalon(), m.getLongueurPantalon(),
                m.getHauteur(), m.getNotes(), m.getDatePrise(), m.isCourante(), m.getCreatedAt());
    }

    @Transactional(readOnly = true)
    public List<com.couturix.commande.dto.CommandeSummaryDto> commandes(Long clientId) {
        require(clientId);
        return commandeRepository.findByClientIdOrderByCreatedAtDesc(clientId).stream()
                .map(commandeService::toSummary)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<PaiementDto> paiements(Long clientId) {
        require(clientId);
        return paiementRepository.findByClientIdOrderByCreatedAtDesc(clientId).stream()
                .map(p -> new PaiementDto(p.getId(), p.getCommande() == null ? null : p.getCommande().getId(),
                        p.getClient().getId(), p.getMontant(), p.getMoyen(), p.getDatePaiement(),
                        p.getReference(), p.getNotes(), p.getCreatedAt()))
                .toList();
    }
}
