package com.huly.backend.domain.useCase.payment;

import com.huly.backend.domain.model.payment.Product;
import com.huly.backend.domain.model.enums.ProductType;
import com.huly.backend.domain.repository.payment.ProductRepository;
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

    @Mock private ProductRepository productRepository;
    @InjectMocks private ListPlansUseCase useCase;

    @Test
    void execute_shouldReturnAllPlans() {
        List<Product> plans = List.of(
                Product.builder().id(10L).name("Plan Premium").description("Acceso premium")
                        .price(new BigDecimal("9999")).coinsAmount(0)
                        .type(ProductType.PLAN).planCode("PREMIUM").build(),
                Product.builder().id(11L).name("Plan Pro").description("Acceso pro")
                        .price(new BigDecimal("4999")).coinsAmount(0)
                        .type(ProductType.PLAN).planCode("PRO").build()
        );
        when(productRepository.findByType(ProductType.PLAN)).thenReturn(plans);

        List<Product> result = useCase.execute();

        assertThat(result).hasSize(2);
        assertThat(result.get(0).getName()).isEqualTo("Plan Premium");
        assertThat(result.get(0).getPlanCode()).isEqualTo("PREMIUM");
        assertThat(result.get(0).getType()).isEqualTo(ProductType.PLAN);
        assertThat(result.get(1).getName()).isEqualTo("Plan Pro");
    }

    @Test
    void execute_shouldReturnEmptyList_whenNoPlansExist() {
        when(productRepository.findByType(ProductType.PLAN)).thenReturn(Collections.emptyList());

        List<Product> result = useCase.execute();

        assertThat(result).isEmpty();
    }

    @Test
    void execute_shouldQueryRepositoryWithPlanType() {
        when(productRepository.findByType(ProductType.PLAN)).thenReturn(Collections.emptyList());

        useCase.execute();

        verify(productRepository).findByType(ProductType.PLAN);
    }
}
