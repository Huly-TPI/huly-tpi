package com.huly.backend.domain.useCase.store;
import com.huly.backend.domain.exception.BusinessRuleException;
import com.huly.backend.domain.model.StoreItem;
import com.huly.backend.domain.model.UserStoreItem;
import com.huly.backend.domain.model.enums.ItemCategory;
import com.huly.backend.domain.repository.StoreItemRepository;
import com.huly.backend.domain.repository.UserStoreItemRepository;
import com.huly.backend.infrastructure.presentation.exception.NotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EquipStoreItemUseCaseTest {

    @Mock 
    private StoreItemRepository storeItemRepository;

    @Mock 
    private UserStoreItemRepository userStoreItemRepository;

    @InjectMocks
    private EquipStoreItemUseCase equipStoreItemUseCase;

    private StoreItem item(Long id, ItemCategory category) {
        return StoreItem.builder() 
                .id(id).name("Item " + id).description("desc")
                .category(category).assetKey("asset-" + id).priceCoins(10).build();
            
    }

    private UserStoreItem userItem(Long itemId, ItemCategory category, boolean equipped) {
        return UserStoreItem.builder() 
                .storeItem(item(itemId, category)).equipped(equipped).build();
    }

    @Test 
    void equip_shouldThrowNotFound_whenItemDoesNotExist() {
        when(storeItemRepository.findById(10L)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> equipStoreItemUseCase.execute(1L, 10L)).isInstanceOf(NotFoundException.class);
    }

    @Test 
    void equip_shouldThrowBusinessRule_whenItemNotOwned() {
        when(storeItemRepository.findById(10L)).thenReturn(Optional.of(item(10L, ItemCategory.HOUSE)));
        when(userStoreItemRepository.isOwned(1L, 10L)).thenReturn(false);
        assertThatThrownBy(() -> equipStoreItemUseCase.execute(1L, 10L)).isInstanceOf(BusinessRuleException.class);
        verify(userStoreItemRepository, never()).updateEquipped(1L, 10L, true);
        }

    @Test 
    void equip_shouldUnequipSameCategoryAndEquipTarget_whenValid() {
        when(storeItemRepository.findById(10L)).thenReturn(Optional.of(item(10L, ItemCategory.HOUSE)));
        when(userStoreItemRepository.isOwned(1L, 10L)).thenReturn(true);
        when(userStoreItemRepository.findAllByUserId(1L)).thenReturn(List.of(userItem(20L, ItemCategory.HOUSE, true), 
        userItem(30L, ItemCategory.TREE, true), 
        userItem(10L, ItemCategory.HOUSE, false)));
        
        equipStoreItemUseCase.execute(1L, 10L);

        verify(userStoreItemRepository).updateEquipped(1L, 20L, false);
        verify(userStoreItemRepository, never()).updateEquipped(1L, 30L, false);
        verify(userStoreItemRepository).updateEquipped(1L, 10L, true);
   
    }


    
}
