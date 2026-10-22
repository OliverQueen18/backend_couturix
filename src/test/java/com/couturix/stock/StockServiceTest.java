package com.couturix.stock;

import com.couturix.admin.ParametreRepository;
import com.couturix.config.CouturixProperties;
import com.couturix.domain.enums.TypeMouvementStock;
import com.couturix.stock.dto.InventaireRequest;
import com.couturix.stock.dto.InventaireResultatDto;
import com.couturix.stock.dto.MouvementRequest;
import com.couturix.user.UserRepository;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class StockServiceTest {

    @Mock ArticleStockRepository articleRepository;
    @Mock MouvementStockRepository mouvementRepository;
    @Mock CategorieStockRepository categorieRepository;
    @Mock ParametreRepository parametreRepository;
    @Mock CouturixProperties properties;
    @Mock CouturixProperties.Stock stockProps;
    @Mock UserRepository userRepository;

    StockService service;

    @BeforeEach
    void init() {
        lenient().when(properties.stock()).thenReturn(stockProps);
        lenient().when(stockProps.allowNegative()).thenReturn(false);
        lenient().when(parametreRepository.findById("stock.allowNegative")).thenReturn(Optional.empty());
        service = new StockService(articleRepository, mouvementRepository, categorieRepository, parametreRepository, properties, userRepository);
    }

    @Test
    void refuseSortieNegative() {
        ArticleStock article = new ArticleStock();
        article.setId(1L);
        article.setNom("Tissu");
        article.setStockActuel(new BigDecimal("2"));
        when(articleRepository.findByIdForUpdate(1L)).thenReturn(Optional.of(article));
        assertThrows(RuntimeException.class, () -> service.mouvement(
                new MouvementRequest(1L, TypeMouvementStock.SORTIE, new BigDecimal("5"), "test", null)));
    }

    @Test
    void inventaireIgnoreLignesSansEcart() {
        ArticleStock article = new ArticleStock();
        article.setId(1L);
        article.setNom("Fil");
        article.setStockActuel(new BigDecimal("10"));
        when(articleRepository.findByIdForUpdate(1L)).thenReturn(Optional.of(article));
        InventaireResultatDto result = service.inventaire(List.of(
                new InventaireRequest(1L, new BigDecimal("10"))));
        assertEquals(1, result.lignesCompte());
        assertEquals(0, result.ajustements());
    }

    @Test
    void inventaireAjusteEcart() {
        ArticleStock article = new ArticleStock();
        article.setId(2L);
        article.setNom("Bouton");
        article.setStockActuel(new BigDecimal("4"));
        when(articleRepository.findByIdForUpdate(2L)).thenReturn(Optional.of(article));
        when(articleRepository.save(any(ArticleStock.class))).thenAnswer(inv -> inv.getArgument(0));
        when(mouvementRepository.save(any(MouvementStock.class))).thenAnswer(inv -> inv.getArgument(0));
        InventaireResultatDto result = service.inventaire(List.of(
                new InventaireRequest(2L, new BigDecimal("6"))));
        assertEquals(1, result.ajustements());
        assertEquals(new BigDecimal("6"), article.getStockActuel());
    }
}
