package com.couturix.modele;

import com.couturix.client.Client;
import com.couturix.client.ClientRepository;
import com.couturix.common.exception.ApiException;
import com.couturix.modele.dto.FavoriDto;
import com.couturix.modele.dto.FavoriRequest;
import com.couturix.security.SecurityUtils;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/favoris")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('CLIENT','SUPER_ADMIN','ADMIN','GERANT','RECEPTIONNISTE')")
public class FavoriController {

    private final FavoriRepository favoriRepository;
    private final ClientRepository clientRepository;
    private final ModeleService modeleService;

    @GetMapping
    @Transactional(readOnly = true)
    public List<FavoriDto> list() {
        return favoriRepository.findByClientId(currentClient().getId()).stream().map(this::toDto).toList();
    }

    @PostMapping
    @Transactional
    public FavoriDto add(@Valid @RequestBody FavoriRequest request) {
        Client client = currentClient();
        Modele modele = modeleService.require(request.modeleId());
        return favoriRepository.findByClientIdAndModeleId(client.getId(), modele.getId())
                .map(this::toDto)
                .orElseGet(() -> {
                    Favori favori = new Favori();
                    favori.setClient(client);
                    favori.setModele(modele);
                    return toDto(favoriRepository.save(favori));
                });
    }

    @DeleteMapping("/{modeleId}")
    @Transactional
    public void remove(@PathVariable Long modeleId) {
        favoriRepository.findByClientIdAndModeleId(currentClient().getId(), modeleId)
                .ifPresent(favoriRepository::delete);
    }

    private Client currentClient() {
        return clientRepository.findByUserId(SecurityUtils.currentUserId())
                .orElseThrow(() -> ApiException.forbidden("Aucun profil client associé"));
    }

    private FavoriDto toDto(Favori favori) {
        String image = favori.getModele().getImages().stream()
                .filter(ModeleImage::isPrincipale)
                .map(ModeleImage::getUrl)
                .findFirst()
                .orElse(favori.getModele().getImages().stream().map(ModeleImage::getUrl).findFirst().orElse(null));
        return new FavoriDto(favori.getId(), favori.getClient().getId(), favori.getModele().getId(),
                favori.getModele().getNom(), image);
    }
}
