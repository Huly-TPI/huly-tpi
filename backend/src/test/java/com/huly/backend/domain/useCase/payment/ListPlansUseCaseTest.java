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
class ListPlansUseCaseTest {

    @Mock
    private ProductRepository productRepository;

    @InjectMocks
    private ListPlansUseCase useCase;

    @Test
    @DisplayName("Devuelve todos los planes")
    void executeShouldReturnAllPlans() {
        givenPlans();

        List<Product> result = listPlans();

        thenReturnsTwoPlans(result);
    }

    @Test
    @DisplayName("Devuelve una lista vacía cuando no hay planes")
    void executeShouldReturnEmptyListWhenNoPlansExist() {
        givenNoPlans();

        List<Product> result = listPlans();

        thenReturnsEmpty(result);
    }

    @Test
    @DisplayName("Consulta el repositorio filtrando por planes")
    void executeShouldQueryRepositoryWithPlanType() {
        givenNoPlans();

        listPlans();

        thenRepositoryQueriedForPlans();
    }

    // --- arrange ---

    private void givenPlans() {
        when(productRepository.findByTypeAndActive(ProductType.PLAN, true)).thenReturn(List.of(
                Product.builder().id(10L).name("Plan Premium").description("Acceso premium")
                        .price(new BigDecimal("9999")).coinsAmount(0)
                        .type(ProductType.PLAN).planCode("PREMIUM").build(),
                Product.builder().id(11L).name("Plan Pro").description("Acceso pro")
                        .price(new BigDecimal("4999")).coinsAmount(0)
                        .type(ProductType.PLAN).planCode("PRO").build()));
    }

    private void givenNoPlans() {
        when(productRepository.findByTypeAndActive(ProductType.PLAN, true)).thenReturn(Collections.emptyList());
    }

    // --- act ---

    private List<Product> listPlans() {
        return useCase.execute();
    }

    // --- assert ---

    private void thenReturnsTwoPlans(List<Product> result) {
        assertThat(result).hasSize(2);
        assertThat(result.get(0).getName()).isEqualTo("Plan Premium");
        assertThat(result.get(0).getPlanCode()).isEqualTo("PREMIUM");
        assertThat(result.get(0).getType()).isEqualTo(ProductType.PLAN);
        assertThat(result.get(1).getName()).isEqualTo("Plan Pro");
    }

    private void thenReturnsEmpty(List<Product> result) {
        assertThat(result).isEmpty();
    }

    private void thenRepositoryQueriedForPlans() {
        verify(productRepository).findByTypeAndActive(ProductType.PLAN, true);
    }
}
