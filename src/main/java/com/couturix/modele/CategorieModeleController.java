package com.couturix.modele;
import com.couturix.modele.dto.CategorieDto;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
@RestController @RequestMapping("/api/v1/categories-modeles") @RequiredArgsConstructor
@PreAuthorize("isAuthenticated()")
public class CategorieModeleController {
  private final ModeleService service;
  @GetMapping public List<CategorieDto> list() { return service.categories(); }
  @PostMapping @PreAuthorize("hasAnyRole('SUPER_ADMIN','ADMIN','GERANT')") public CategorieDto save(@RequestBody CategorieDto dto) { return service.saveCategorie(dto); }
}
