package com.huly.backend.domain.useCase.payment;

import com.huly.backend.domain.model.payment.Product;
import com.huly.backend.domain.model.enums.ProductType;
import com.huly.backend.domain.repository.payment.ProductRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ListProductsUseCaseTest {

    @Mock
    private ProductRepository productRepository;

    @InjectMocks
    private ListProductsUseCase useCase;

    @Test
    @DisplayName("Devuelve todos los packs de monedas")
    void executeShouldReturnAllProducts() {
        givenCoinPacks();

        List<Product> result = listProducts();

        thenReturnsThreeCoinPacks(result);
    }

    @Test
    @DisplayName("Devuelve una lista vacía cuando no hay packs de monedas")
    void executeShouldReturnEmptyListWhenNoProductsExist() {
        givenNoCoinPacks();

        List<Product> result = listProducts();

        thenReturnsEmpty(result);
    }

    @Test
    @DisplayName("Consulta el repositorio filtrando por packs de monedas")
    void executeShouldDelegateToRepository() {
        givenNoCoinPacks();

        listProducts();

        thenRepositoryQueriedForCoinPacks();
    }

    // --- arrange ---

    private void givenCoinPacks() {
        when(productRepository.findByType(ProductType.COIN_PACK)).thenReturn(List.of(
                Product.builder().id(1L).name("Pack Inicial").description("100 monedas")
                        .price(new BigDecimal("499")).coinsAmount(100).build(),
                Product.builder().id(2L).name("Pack Estándar").description("500 monedas")
                        .price(new BigDecimal("1999")).coinsAmount(500).build(),
                Product.builder().id(3L).name("Pack Premium").description("1500 monedas")
                        .price(new BigDecimal("4999")).coinsAmount(1500).build()));
    }

    private void givenNoCoinPacks() {
        when(productRepository.findByType(ProductType.COIN_PACK)).thenReturn(Collections.emptyList());
    }

    // --- act ---

    private List<Product> listProducts() {
        return useCase.execute();
    }

    // --- assert ---

    private void thenReturnsThreeCoinPacks(List<Product> result) {
        assertThat(result).hasSize(3);
        assertThat(result.get(0).getName()).isEqualTo("Pack Inicial");
        assertThat(result.get(0).getCoinsAmount()).isEqualTo(100);
        assertThat(result.get(1).getName()).isEqualTo("Pack Estándar");
        assertThat(result.get(2).getName()).isEqualTo("Pack Premium");
    }

    private void thenReturnsEmpty(List<Product> result) {
        assertThat(result).isEmpty();
    }

    private void thenRepositoryQueriedForCoinPacks() {
        verify(productRepository).findByType(ProductType.COIN_PACK);
    }
}
