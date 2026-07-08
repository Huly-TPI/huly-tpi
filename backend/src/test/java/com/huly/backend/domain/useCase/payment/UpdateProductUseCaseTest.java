package com.huly.backend.domain.useCase.payment;

import com.huly.backend.domain.dto.payment.UpdateProductRequest;
import com.huly.backend.domain.model.enums.ProductType;
import com.huly.backend.domain.model.payment.Product;
import com.huly.backend.domain.repository.payment.ProductRepository;
import com.huly.backend.infrastructure.presentation.exception.NotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UpdateProductUseCaseTest {

    @Mock private ProductRepository productRepository;
    private UpdateProductUseCase useCase;

    @BeforeEach
    void setUp() { useCase = new UpdateProductUseCase(productRepository); }

    private UpdateProductRequest request() {
        return new UpdateProductRequest(5L, "Nuevo", "desc", new BigDecimal("999"),
                200, ProductType.COIN_PACK, null, null, null);
    }

    @Test
    void execute_shouldThrow_whenNotFound() {
        when(productRepository.findById(5L)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> useCase.execute(request()))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    void execute_shouldUpdateFieldsAndKeepActive() {
        Product existing = Product.builder().id(5L).name("Viejo").price(new BigDecimal("1"))
                .coinsAmount(50).type(ProductType.COIN_PACK).active(true).build();
        when(productRepository.findById(5L)).thenReturn(Optional.of(existing));
        when(productRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        Product result = useCase.execute(request());

        assertThat(result.getName()).isEqualTo("Nuevo");
        assertThat(result.getPrice()).isEqualByComparingTo("999");
        assertThat(result.getCoinsAmount()).isEqualTo(200);
        assertThat(result.isActive()).isTrue();
    }
}