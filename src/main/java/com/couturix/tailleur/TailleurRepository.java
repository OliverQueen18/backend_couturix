package com.couturix.tailleur;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
public interface TailleurRepository extends JpaRepository<Tailleur, Long> {
  Optional<Tailleur> findByUserId(Long userId);
}
