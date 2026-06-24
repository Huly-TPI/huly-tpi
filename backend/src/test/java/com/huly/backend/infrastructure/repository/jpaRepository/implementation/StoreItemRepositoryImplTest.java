package com.huly.backend.infrastructure.repository.jpaRepository.implementation;

import com.huly.backend.domain.model.shop.StoreItem;
import com.huly.backend.domain.model.enums.ItemCategory;
import com.huly.backend.infrastructure.repository.entity.StoreItemEntity;
import com.huly.backend.infrastructure.repository.jpaRepository.interfaces.IStoreItemJpaRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class StoreItemRepositoryImplTest {

    @Mock
    private IStoreItemJpaRepository storeItemJpaRepository;

    @InjectMocks
    private StoreItemRepositoryImpl storeItemRepository;

    private StoreItemEntity entity() {
        return StoreItemEntity.builder()
                .id(10L).name("Casa rosa").description("Casa de color rosa")
                .category(ItemCategory.HOUSE).assetKey("casa-rosa").priceCoins(50)
                .price(new BigDecimal("1000.00"))
                .build();
    }

    @Test
    void findAll_shouldReturnMappedList() {
        StoreItemEntity entity = entity();
        when(storeItemJpaRepository.findAllByOrderByIdAsc()).thenReturn(List.of(entity));
        List<StoreItem> result = storeItemRepository.findAll();
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getId()).isEqualTo(10L);
        assertThat(result.get(0).getName()).isEqualTo("Casa rosa");
        assertThat(result.get(0).getAssetKey()).isEqualTo("casa-rosa");
        assertThat(result.get(0).getCategory()).isEqualTo(ItemCategory.HOUSE);
        assertThat(result.get(0).getPriceCoins()).isEqualTo(50);
        assertThat(result.get(0).getPriceCoins()).isEqualTo(50);
        assertThat(result.get(0).getPrice()).isEqualByComparingTo("1000.00");
    }

    @Test
    void findById_shouldReturnMappedItem() {
        when(storeItemJpaRepository.findById(10L)).thenReturn(Optional.of(entity()));
        Optional<StoreItem> result = storeItemRepository.findById(10L);
        assertThat(result).isPresent();
        assertThat(result.get().getName()).isEqualTo("Casa rosa");
    }

    @Test
    void findById_shouldReturnEmpty_whenNotFound() {
        when(storeItemJpaRepository.findById(99L)).thenReturn(Optional.empty());

        assertThat(storeItemRepository.findById(99L)).isEmpty();
    }

}
