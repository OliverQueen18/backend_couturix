package com.couturix.atelier;

import com.couturix.atelier.dto.AtelierDto;
import com.couturix.atelier.dto.AtelierRequest;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
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
@RequestMapping("/api/v1/ateliers")
@RequiredArgsConstructor
@Tag(name = "Ateliers")
@PreAuthorize("hasAnyRole('SUPER_ADMIN','ADMIN','GERANT')")
public class AtelierController {

    private final AtelierService atelierService;

    @GetMapping
    public List<AtelierDto> tree() {
        return atelierService.tree();
    }

    @GetMapping("/courant")
    public AtelierDto current() {
        return atelierService.current();
    }

    @GetMapping("/{id}")
    public AtelierDto get(@PathVariable Long id) {
        return atelierService.get(id);
    }

    @PostMapping
    public AtelierDto create(@Valid @RequestBody AtelierRequest request) {
        return atelierService.create(request);
    }

    @PutMapping("/{id}")
    public AtelierDto update(@PathVariable Long id, @Valid @RequestBody AtelierRequest request) {
        return atelierService.update(id, request);
    }

    @PostMapping(value = "/{id}/logo", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public AtelierDto logo(@PathVariable Long id, @RequestParam("file") MultipartFile file) {
        return atelierService.uploadLogo(id, file);
    }
}
