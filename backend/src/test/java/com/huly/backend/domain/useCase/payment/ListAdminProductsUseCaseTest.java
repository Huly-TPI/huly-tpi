package com.huly.backend.domain.useCase.payment;

import com.huly.backend.domain.model.enums.ProductType;
import com.huly.backend.domain.model.payment.Product;
import com.huly.backend.domain.repository.payment.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ListAdminProductsUseCaseTest {

    @Mock private ProductRepository productRepository;
    private ListAdminProductsUseCase useCase;

    @BeforeEach
    void setUp() { useCase = new ListAdminProductsUseCase(productRepository); }

    @Test
    void execute_shouldReturnAllProductsOfType_includingInactive() {
        Product active = Product.builder().id(1L).type(ProductType.COIN_PACK).active(true).build();
        Product inactive = Product.builder().id(2L).type(ProductType.COIN_PACK).active(false).build();
        when(productRepository.findByType(ProductType.COIN_PACK)).thenReturn(List.of(active, inactive));

        List<Product> result = useCase.execute(ProductType.COIN_PACK);

        assertThat(result).hasSize(2);
        verify(productRepository).findByType(ProductType.COIN_PACK);
    }
}