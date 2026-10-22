package com.couturix.composition;
import java.util.List; import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
public interface CompositionRepository extends JpaRepository<Composition, Long> {
  List<Composition> findByModeleIdOrderByVersionDesc(Long modeleId);
  Optional<Composition> findFirstByModeleIdAndActiveTrue(Long modeleId);
}
