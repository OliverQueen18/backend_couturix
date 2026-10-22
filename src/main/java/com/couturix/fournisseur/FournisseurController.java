package com.couturix.fournisseur;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
@RestController @RequestMapping("/api/v1/fournisseurs") @RequiredArgsConstructor
@PreAuthorize("hasAnyRole('SUPER_ADMIN','ADMIN','GERANT','MAGASINIER','COMPTABLE')")
public class FournisseurController {
  private final FournisseurRepository repo;
  @GetMapping public List<Fournisseur> list() { return repo.findAll(); }
  @PostMapping public Fournisseur create(@RequestBody Fournisseur f) { return repo.save(f); }
  @PutMapping("/{id}") public Fournisseur update(@PathVariable Long id, @RequestBody Fournisseur f) { f.setId(id); return repo.save(f); }
}
