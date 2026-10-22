package com.couturix.stock;
import jakarta.persistence.LockModeType;
import java.util.List; import java.util.Optional;
import org.springframework.data.jpa.repository.*;
public interface ArticleStockRepository extends JpaRepository<ArticleStock, Long> {
  @Lock(LockModeType.PESSIMISTIC_WRITE) @Query("select a from ArticleStock a where a.id = :id")
  Optional<ArticleStock> findByIdForUpdate(Long id);
  @Query("select a from ArticleStock a where a.stockActuel <= a.stockMinimum and a.actif = true")
  List<ArticleStock> findAlertes();
}
