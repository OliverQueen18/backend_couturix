package com.couturix.client;

import com.couturix.client.dto.ClientDetailDto;
import com.couturix.client.dto.ClientDto;
import com.couturix.client.dto.ClientRequest;
import com.couturix.client.dto.MesureDto;
import com.couturix.commande.dto.CommandeSummaryDto;
import com.couturix.common.api.PageResponse;
import com.couturix.paiement.dto.PaiementDto;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/v1/clients")
@RequiredArgsConstructor
@Tag(name = "Clients")
@PreAuthorize("hasAnyRole('SUPER_ADMIN','ADMIN','GERANT','RECEPTIONNISTE','CAISSIER','COMPTABLE')")
public class ClientController {

    private final ClientService clientService;

    @GetMapping
    public PageResponse<ClientDto> search(@RequestParam(required = false) String q, Pageable pageable) {
        return clientService.search(q, pageable);
    }

    @GetMapping("/{id}")
    public ClientDetailDto get(@PathVariable Long id) {
        return clientService.get(id);
    }

    @PostMapping
    public ClientDto create(@Valid @RequestBody ClientRequest request) {
        return clientService.create(request);
    }

    @PutMapping("/{id}")
    public ClientDto update(@PathVariable Long id, @Valid @RequestBody ClientRequest request) {
        return clientService.update(id, request);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        clientService.delete(id);
    }

    @GetMapping("/{id}/mesures")
    public List<MesureDto> mesures(@PathVariable Long id) {
        return clientService.mesures(id);
    }

    @PostMapping("/{id}/mesures")
    public MesureDto addMesure(@PathVariable Long id, @RequestBody MesureDto dto) {
        return clientService.addMesure(id, dto);
    }

    @PostMapping("/{id}/photo")
    public ClientDto photo(@PathVariable Long id, @RequestParam("file") MultipartFile file) {
        return clientService.uploadPhoto(id, file);
    }

    @GetMapping("/{id}/commandes")
    public List<CommandeSummaryDto> commandes(@PathVariable Long id) {
        return clientService.commandes(id);
    }

    @GetMapping("/{id}/paiements")
    public List<PaiementDto> paiements(@PathVariable Long id) {
        return clientService.paiements(id);
    }
}
