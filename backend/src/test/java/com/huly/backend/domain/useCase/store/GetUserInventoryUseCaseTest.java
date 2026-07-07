package com.huly.backend.domain.useCase.store;

import com.huly.backend.domain.dto.store.GetUserInventoryRequest;
import com.huly.backend.domain.dto.store.GetUserInventoryResponse;
import com.huly.backend.domain.mapper.store.GetUserInventoryMapper;
import com.huly.backend.domain.model.enums.ItemCategory;
import com.huly.backend.domain.model.shop.StoreItem;
import com.huly.backend.domain.model.user.UserStoreItem;
import com.huly.backend.domain.repository.UserStoreItemRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GetUserInventoryUseCaseTest {

    private static final Long USER_ID = 7L;

    @Mock
    private UserStoreItemRepository userStoreItemRepository;

    private GetUserInventoryUseCase getUserInventoryUseCase;

    @BeforeEach
    void setUp() {
        getUserInventoryUseCase = new GetUserInventoryUseCase(userStoreItemRepository, new GetUserInventoryMapper());
    }

    @Test
    @DisplayName("Devuelve el inventario del usuario mapeado desde el repositorio")
    void executeShouldReturnUserInventory() {
        givenUserOwnsEquippedHouse();

        GetUserInventoryResponse result = getInventory();

        thenResponseContainsEquippedHouse(result);
    }

    // --- arrange ---

    private void givenUserOwnsEquippedHouse() {
        StoreItem storeItem = StoreItem.builder()
                .id(5L).name("Casa rosa").category(ItemCategory.HOUSE).assetKey("casa-rosa").build();
        UserStoreItem item = UserStoreItem.builder().id(1L).userId(USER_ID).storeItem(storeItem).equipped(true).build();
        when(userStoreItemRepository.findAllByUserId(USER_ID)).thenReturn(List.of(item));
    }

    // --- act ---

    private GetUserInventoryResponse getInventory() {
        return getUserInventoryUseCase.execute(new GetUserInventoryRequest(USER_ID));
    }

    // --- assert ---

    private void thenResponseContainsEquippedHouse(GetUserInventoryResponse result) {
        assertThat(result.items()).hasSize(1);
        assertThat(result.items().get(0).storeItemId()).isEqualTo(5L);
        assertThat(result.items().get(0).equipped()).isTrue();
        verify(userStoreItemRepository).findAllByUserId(USER_ID);
    }
}
