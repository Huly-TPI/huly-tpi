package com.huly.backend.domain.useCase.store;
import com.huly.backend.domain.model.StoreItem;
import com.huly.backend.domain.model.enums.ItemCategory;
import com.huly.backend.domain.repository.StoreItemRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ListStoreItemsUseCaseTest {

    @Mock
    private StoreItemRepository storeItemRepository;

    @InjectMocks
    private ListStoreItemsUseCase listStoreItemsUseCase;

    @Test 
    void execute_shouldReturnAllItemsFromRepository() {
        StoreItem casa = StoreItem.builder()
                .id(1L)
                .name("Casa rosa")
                .description("Casa de color rosa")
                .category(ItemCategory.HOUSE)
                .assetKey("casa-rosa")
                .priceCoins(50)
                .build();
        StoreItem maceta = StoreItem.builder()
                .id(2L)
                .name("Maceta lila")
                .description("Maceta en tono lila")
                .category(ItemCategory.TREE)
                .assetKey("maceta-lila")
                .priceCoins(30)
                .build();
        when(storeItemRepository.findAll()).thenReturn(List.of(casa, maceta));
        List<StoreItem> result = listStoreItemsUseCase.execute();
        assertThat(result).containsExactly(casa, maceta);
    }
    
}
