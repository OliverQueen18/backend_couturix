package com.couturix.ticket;

import com.couturix.common.exception.ApiException;
import com.couturix.ticket.dto.TicketSignataireRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertThrows;

@ExtendWith(MockitoExtension.class)
class TicketSignataireServiceTest {

    @Mock TicketSignataireRepository repo;

    TicketSignataireService service;

    @BeforeEach
    void init() {
        service = new TicketSignataireService(repo);
    }

    @Test
    void refuseSansAtelier() {
        TicketSignataireRequest request = new TicketSignataireRequest("Le gérant", "Aminata Traoré", 1, true);
        assertThrows(ApiException.class, () -> service.create(request));
    }
}
