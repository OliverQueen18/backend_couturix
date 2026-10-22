package com.couturix.admin;
import com.couturix.common.api.PageResponse;
import com.couturix.common.audit.HistoriqueAction;
import com.couturix.common.audit.HistoriqueActionRepository;
import java.time.Instant; import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
@RestController @RequiredArgsConstructor
@PreAuthorize("hasAnyRole('SUPER_ADMIN','ADMIN')")
public class AdminController {
  private final ParametreRepository parametreRepository;
  private final HistoriqueActionRepository historiqueActionRepository;
  @GetMapping("/api/v1/parametres") public List<Parametre> params() { return parametreRepository.findAll(); }
  @PutMapping("/api/v1/parametres/{cle}") public Parametre update(@PathVariable String cle, @RequestBody Parametre body) {
    Parametre p = parametreRepository.findById(cle).orElseGet(() -> { Parametre n=new Parametre(); n.setCle(cle); return n; });
    p.setValeur(body.getValeur()); p.setDescription(body.getDescription()); p.setUpdatedAt(Instant.now());
    return parametreRepository.save(p);
  }
  @GetMapping("/api/v1/audit") public PageResponse<HistoriqueAction> audit(Pageable pageable) { return PageResponse.of(historiqueActionRepository.findAll(pageable)); }
}
