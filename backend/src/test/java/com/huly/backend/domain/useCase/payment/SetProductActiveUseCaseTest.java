package com.huly.backend.domain.useCase.payment;

import com.huly.backend.domain.model.enums.ProductType;
import com.huly.backend.domain.model.payment.Product;
import com.huly.backend.domain.repository.payment.ProductRepository;
import com.huly.backend.infrastructure.presentation.exception.NotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SetProductActiveUseCaseTest {

    @Mock private ProductRepository productRepository;
    private SetProductActiveUseCase useCase;

    @BeforeEach
    void setUp() { useCase = new SetProductActiveUseCase(productRepository); }

    @Test
    void execute_shouldThrow_whenNotFound() {
        when(productRepository.findById(9L)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> useCase.execute(9L, false))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    void execute_shouldToggleActive() {
        Product existing = Product.builder().id(9L).name("Pack").type(ProductType.COIN_PACK).active(true).build();
        when(productRepository.findById(9L)).thenReturn(Optional.of(existing));
        when(productRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        Product result = useCase.execute(9L, false);

        assertThat(result.isActive()).isFalse();
    }
}