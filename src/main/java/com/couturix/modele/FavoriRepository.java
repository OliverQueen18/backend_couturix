package com.couturix.modele;
import java.util.List; import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
public interface FavoriRepository extends JpaRepository<Favori, Long> {
  List<Favori> findByClientId(Long clientId);
  Optional<Favori> findByClientIdAndModeleId(Long clientId, Long modeleId);
  void deleteByClientIdAndModeleId(Long clientId, Long modeleId);
}
