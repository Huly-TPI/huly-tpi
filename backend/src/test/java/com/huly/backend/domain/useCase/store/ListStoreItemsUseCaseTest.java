package com.huly.backend.domain.useCase.store;

import com.huly.backend.domain.dto.store.ListStoreItemsResponse;
import com.huly.backend.domain.mapper.store.ListStoreItemsMapper;
import com.huly.backend.domain.model.enums.ItemCategory;
import com.huly.backend.domain.model.shop.StoreItem;
import com.huly.backend.domain.repository.StoreItemRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ListStoreItemsUseCaseTest {

    @Mock
    private StoreItemRepository storeItemRepository;

    private ListStoreItemsUseCase listStoreItemsUseCase;

    @BeforeEach
    void setUp() {
        listStoreItemsUseCase = new ListStoreItemsUseCase(storeItemRepository, new ListStoreItemsMapper());
    }

    @Test
    @DisplayName("Devuelve todos los items provistos por el repositorio")
    void executeShouldReturnAllItemsFromRepository() {
        givenStoreHasHouseAndTree();

        ListStoreItemsResponse result = list();

        thenResponseContainsHouseAndTree(result);
    }

    // --- arrange ---

    private void givenStoreHasHouseAndTree() {
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
    }

    // --- act ---

    private ListStoreItemsResponse list() {
        return listStoreItemsUseCase.execute();
    }

    // --- assert ---

    private void thenResponseContainsHouseAndTree(ListStoreItemsResponse result) {
        assertThat(result.items()).hasSize(2);
        assertThat(result.items().get(0).id()).isEqualTo(1L);
        assertThat(result.items().get(0).name()).isEqualTo("Casa rosa");
        assertThat(result.items().get(0).category()).isEqualTo(ItemCategory.HOUSE);
        assertThat(result.items().get(1).id()).isEqualTo(2L);
        assertThat(result.items().get(1).category()).isEqualTo(ItemCategory.TREE);
    }
}
