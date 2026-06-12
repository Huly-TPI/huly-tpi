package com.huly.backend.infrastructure.repository.jpaRepository.implementation;

import com.huly.backend.domain.dto.payment.Product;
import com.huly.backend.domain.model.enums.ProductType;
import com.huly.backend.infrastructure.repository.entity.ProductEntity;
import com.huly.backend.infrastructure.repository.jpaRepository.interfaces.IProductJpaRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProductRepositoryImplTest {

    @Mock private IProductJpaRepository jpaRepository;
    @InjectMocks private ProductRepositoryImpl repository;

    private ProductEntity coinPackEntity() {
        return ProductEntity.builder()
                .id(1L).name("Pack Inicial").description("100 monedas")
                .price(new BigDecimal("499")).coinsAmount(100)
                .type(ProductType.COIN_PACK).planCode(null)
                .build();
    }

    private ProductEntity planEntity() {
        return ProductEntity.builder()
                .id(10L).name("Plan Premium").description("Acceso premium")
                .price(new BigDecimal("9999")).coinsAmount(0)
                .type(ProductType.PLAN).planCode("PREMIUM").chatDailyLimit(20)
                .build();
    }

    @Test
    void findAll_shouldMapAllEntitiesToDomain() {
        when(jpaRepository.findAll()).thenReturn(List.of(coinPackEntity(), planEntity()));

        List<Product> result = repository.findAll();

        assertThat(result).hasSize(2);
        Product first = result.get(0);
        assertThat(first.getId()).isEqualTo(1L);
        assertThat(first.getName()).isEqualTo("Pack Inicial");
        assertThat(first.getDescription()).isEqualTo("100 monedas");
        assertThat(first.getPrice()).isEqualByComparingTo("499");
        assertThat(first.getCoinsAmount()).isEqualTo(100);
        assertThat(first.getType()).isEqualTo(ProductType.COIN_PACK);
        assertThat(first.getPlanCode()).isNull();
    }

    @Test
    void findAll_shouldReturnEmptyList_whenNoEntities() {
        when(jpaRepository.findAll()).thenReturn(List.of());

        assertThat(repository.findAll()).isEmpty();
    }

    @Test
    void findById_shouldReturnMappedProduct_whenFound() {
        when(jpaRepository.findById(10L)).thenReturn(Optional.of(planEntity()));

        Optional<Product> result = repository.findById(10L);

        assertThat(result).isPresent();
        assertThat(result.get().getId()).isEqualTo(10L);
        assertThat(result.get().getType()).isEqualTo(ProductType.PLAN);
        assertThat(result.get().getPlanCode()).isEqualTo("PREMIUM");
        assertThat(result.get().getChatDailyLimit()).isEqualTo(20);
    }

    @Test
    void findById_shouldReturnEmpty_whenNotFound() {
        when(jpaRepository.findById(99L)).thenReturn(Optional.empty());

        assertThat(repository.findById(99L)).isEmpty();
    }

    @Test
    void findByType_shouldReturnMappedProductsOfType() {
        when(jpaRepository.findByType(ProductType.PLAN)).thenReturn(List.of(planEntity()));

        List<Product> result = repository.findByType(ProductType.PLAN);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getType()).isEqualTo(ProductType.PLAN);
        assertThat(result.get(0).getName()).isEqualTo("Plan Premium");
    }

    @Test
    void findByType_shouldReturnEmptyList_whenNoneOfType() {
        when(jpaRepository.findByType(ProductType.COIN_PACK)).thenReturn(List.of());

        assertThat(repository.findByType(ProductType.COIN_PACK)).isEmpty();
    }
}
