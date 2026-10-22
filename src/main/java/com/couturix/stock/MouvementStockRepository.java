package com.couturix.stock;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
public interface MouvementStockRepository extends JpaRepository<MouvementStock, Long> {
  List<MouvementStock> findAllByOrderByCreatedAtDesc();
  List<MouvementStock> findByArticleIdOrderByCreatedAtDesc(Long articleId);
}
