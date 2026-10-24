package com.couturix.ticket;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TicketSignataireRepository extends JpaRepository<TicketSignataire, Long> {
    List<TicketSignataire> findAllByOrderByOrdreAscIdAsc();
}
