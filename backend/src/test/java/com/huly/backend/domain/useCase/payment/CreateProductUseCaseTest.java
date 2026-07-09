package com.huly.backend.domain.useCase.payment;

import com.huly.backend.domain.dto.payment.CreateProductRequest;
import com.huly.backend.domain.model.enums.ProductType;
import com.huly.backend.domain.model.payment.Product;
import com.huly.backend.domain.repository.payment.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CreateProductUseCaseTest {

    @Mock private ProductRepository productRepository;
    private CreateProductUseCase useCase;

    @BeforeEach
    void setUp() { useCase = new CreateProductUseCase(productRepository); }

    @Test
    void execute_shouldSaveActiveProduct() {
        when(productRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        useCase.execute(new CreateProductRequest("Pack", "100 semillas",
                new BigDecimal("499"), 100, ProductType.COIN_PACK, null, null, null));

        ArgumentCaptor<Product> captor = ArgumentCaptor.forClass(Product.class);
        verify(productRepository).save(captor.capture());
        assertThat(captor.getValue().getName()).isEqualTo("Pack");
        assertThat(captor.getValue().getCoinsAmount()).isEqualTo(100);
        assertThat(captor.getValue().isActive()).isTrue();
    }
}