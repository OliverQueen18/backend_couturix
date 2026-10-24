package com.couturix.ticket;

import com.couturix.ticket.dto.TicketSignataireDto;
import com.couturix.ticket.dto.TicketSignataireRequest;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/ticket-signataires")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('SUPER_ADMIN','ADMIN','GERANT','CAISSIER','COMPTABLE')")
public class TicketSignataireController {

    private final TicketSignataireService service;

    @GetMapping
    public List<TicketSignataireDto> list() {
        return service.list();
    }

    @PostMapping
    public TicketSignataireDto create(@Valid @RequestBody TicketSignataireRequest request) {
        return service.create(request);
    }

    @PutMapping("/{id}")
    public TicketSignataireDto update(@PathVariable Long id, @Valid @RequestBody TicketSignataireRequest request) {
        return service.update(id, request);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        service.delete(id);
    }
}
