package com.couturix.modele;
import com.couturix.commande.CommandeService;
import com.couturix.commande.dto.CommandeSummaryDto;
import com.couturix.common.api.PageResponse;
import com.couturix.domain.enums.ModeleStatut;
import com.couturix.modele.dto.CategorieDto;
import com.couturix.modele.dto.DemandeRequest;
import com.couturix.modele.dto.ModeleDto;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;

@RestController @RequestMapping("/api/v1/public") @RequiredArgsConstructor
public class PublicModeleController {
  private final ModeleService modeleService;
  private final CommandeService commandeService;

  @GetMapping("/modeles") public PageResponse<ModeleDto> modeles(@RequestParam(required=false) String q, Pageable pageable) {
    return modeleService.list(ModeleStatut.PUBLIE, q, pageable);
  }
  @GetMapping("/modeles/{id}") public ModeleDto modele(@PathVariable Long id) { return modeleService.get(id); }
  @GetMapping("/categories") public List<CategorieDto> categories() { return modeleService.categories(); }

  @PostMapping("/demandes")
  public CommandeSummaryDto demande(@Valid @RequestBody DemandeRequest request) {
    return commandeService.createFromPublicDemande(request);
  }
}
