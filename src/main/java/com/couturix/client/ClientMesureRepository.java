package com.couturix.client;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ClientMesureRepository extends JpaRepository<ClientMesure, Long> {
    List<ClientMesure> findByClientIdOrderByDatePriseDesc(Long clientId);

    Optional<ClientMesure> findFirstByClientIdAndCouranteTrue(Long clientId);

    @Modifying
    @Query("UPDATE ClientMesure m SET m.courante = false WHERE m.client.id = :clientId AND m.courante = true")
    void markPreviousNotCourante(@Param("clientId") Long clientId);
}
