package com.huly.backend.presentation.controller;
import com.huly.backend.domain.model.StoreItem;
import com.huly.backend.domain.model.UserStoreItem;
import com.huly.backend.domain.model.enums.ItemCategory;
import com.huly.backend.domain.useCase.store.BuyStoreItemUseCase;
import com.huly.backend.domain.useCase.store.EquipStoreItemUseCase;
import com.huly.backend.domain.useCase.store.GetUserInventoryUseCase;
import com.huly.backend.domain.useCase.store.ListStoreItemsUseCase;
import com.huly.backend.domain.useCase.store.UnequipStoreItemUseCase;
import com.huly.backend.infrastructure.presentation.controller.StoreController;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class StoreControllerTest {

    @InjectMocks
    private StoreController storeController;

    @Mock
    private BuyStoreItemUseCase buyStoreItemUseCase;

    @Mock
    private GetUserInventoryUseCase getUserInventoryUseCase;

    @Mock
    private ListStoreItemsUseCase listStoreItemsUseCase;

    @Mock
    private EquipStoreItemUseCase equipStoreItemUseCase;

    @Mock
    private UnequipStoreItemUseCase unequipStoreItemUseCase;

    @Mock private UserDetails userDetails;

    @Test 
    void getItems_shouldReturnMappedCatalog() {
        StoreItem item = StoreItem.builder() 
                .id(10L).name("Casa rosa").description("Casa de color rosa")
                .category(ItemCategory.HOUSE).assetKey("casa-rosa").priceCoins(50)
                .build();
        when(listStoreItemsUseCase.execute()).thenReturn(List.of(item));
        var response = storeController.getItems();

        assertThat(response.getStatusCode().is2xxSuccessful()).isTrue();
        assertThat(response.getBody()).hasSize(1);
        assertThat(response.getBody().get(0).assetKey()).isEqualTo("casa-rosa");
        assertThat(response.getBody().get(0).category()).isEqualTo("HOUSE");
        assertThat(response.getBody().get(0).priceCoins()).isEqualTo(50);
    }

    @Test
    void getInventory_shouldReturnMappedInventory() {
        StoreItem item = StoreItem.builder()
                .id(10L).name("Casa rosa").description("desc")
                .category(ItemCategory.HOUSE).assetKey("casa-rosa").priceCoins(50)
                .build();
        UserStoreItem owned = UserStoreItem.builder().id(1L).userId(7L).storeItem(item).equipped(true).build();
        when(userDetails.getUsername()).thenReturn("7");
        when(getUserInventoryUseCase.execute(7L)).thenReturn(List.of(owned));

        var response = storeController.getMyInventory(userDetails);

        assertThat(response.getStatusCode().is2xxSuccessful()).isTrue();
        assertThat(response.getBody()).hasSize(1);
        assertThat(response.getBody().get(0).storeItemId()).isEqualTo(10L);
        assertThat(response.getBody().get(0).assetKey()).isEqualTo("casa-rosa");
        assertThat(response.getBody().get(0).equipped()).isTrue();
    }

    @Test 
    void buy_shouldDelegateToUseCaseAndReturnOk() {
        when(userDetails.getUsername()).thenReturn("7");
        var response = storeController.buyItem(10L, userDetails);
        assertThat(response.getStatusCode().is2xxSuccessful()).isTrue();
        verify(buyStoreItemUseCase).execute(7L, 10L);
    }

    @Test 
    void equip_shouldDelegateToUseCaseAndReturnOk() {
        when(userDetails.getUsername()).thenReturn("7");
        var response = storeController.equipItem(10L, userDetails);
        assertThat(response.getStatusCode().is2xxSuccessful()).isTrue();
        verify(equipStoreItemUseCase).execute(7L, 10L);
    }

    @Test
    void unequip_shouldDelegateToUseCaseAndReturnOk() {
        when(userDetails.getUsername()).thenReturn("7");
        var response = storeController.unequipItem(10L, userDetails);
        assertThat(response.getStatusCode().is2xxSuccessful()).isTrue();
        verify(unequipStoreItemUseCase).execute(7L, 10L);
    }
    
}
