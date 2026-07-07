package com.huly.backend.domain.useCase.store;

import com.huly.backend.domain.dto.store.EquipStoreItemRequest;
import com.huly.backend.domain.dto.store.EquipStoreItemResponse;
import com.huly.backend.domain.exception.BusinessRuleException;
import com.huly.backend.domain.mapper.store.EquipStoreItemMapper;
import com.huly.backend.domain.model.enums.ItemCategory;
import com.huly.backend.domain.model.shop.StoreItem;
import com.huly.backend.domain.model.user.UserStoreItem;
import com.huly.backend.domain.repository.StoreItemRepository;
import com.huly.backend.domain.repository.UserStoreItemRepository;
import com.huly.backend.infrastructure.presentation.exception.NotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EquipStoreItemUseCaseTest {

    private static final Long USER_ID = 1L;
    private static final Long ITEM_ID = 10L;

    @Mock
    private StoreItemRepository storeItemRepository;

    @Mock
    private UserStoreItemRepository userStoreItemRepository;

    private EquipStoreItemUseCase equipStoreItemUseCase;

    @BeforeEach
    void setUp() {
        equipStoreItemUseCase = new EquipStoreItemUseCase(
                storeItemRepository, userStoreItemRepository, new EquipStoreItemMapper());
    }

    @Test
    @DisplayName("Lanza NotFound cuando el item no existe")
    void equipShouldThrowNotFoundWhenItemDoesNotExist() {
        givenItemDoesNotExist();

        thenEquipThrowsNotFound();
    }

    @Test
    @DisplayName("Lanza error de negocio y no equipa cuando el usuario no posee el item")
    void equipShouldThrowBusinessRuleWhenItemNotOwned() {
        givenItemExists(ItemCategory.HOUSE);
        givenItemNotOwned();

        thenEquipThrowsBusinessRule();
        thenTargetWasNotEquipped();
    }

    @Test
    @DisplayName("Desequipa el item equipado de la misma categoría y equipa el objetivo")
    void equipShouldUnequipSameCategoryAndEquipTargetWhenValid() {
        givenItemExists(ItemCategory.HOUSE);
        givenItemOwned();
        givenInventory(List.of(
                userItem(20L, ItemCategory.HOUSE, true),
                userItem(30L, ItemCategory.TREE, true),
                userItem(ITEM_ID, ItemCategory.HOUSE, false)));

        EquipStoreItemResponse result = equip();

        thenOtherHouseUnequippedAndTreeUntouched();
        thenTargetEquipped(result);
    }

    @Test
    @DisplayName("No desequipa al objetivo aunque ya esté equipado en su categoría")
    void equipShouldNotUnequipTargetItselfWhenAlreadyEquipped() {
        givenItemExists(ItemCategory.HOUSE);
        givenItemOwned();
        givenInventory(List.of(userItem(ITEM_ID, ItemCategory.HOUSE, true)));

        EquipStoreItemResponse result = equip();

        thenTargetWasNotUnequipped();
        thenTargetEquipped(result);
    }

    // --- arrange ---

    private void givenItemDoesNotExist() {
        when(storeItemRepository.findById(ITEM_ID)).thenReturn(Optional.empty());
    }

    private void givenItemExists(ItemCategory category) {
        when(storeItemRepository.findById(ITEM_ID)).thenReturn(Optional.of(item(ITEM_ID, category)));
    }

    private void givenItemOwned() {
        when(userStoreItemRepository.isOwned(USER_ID, ITEM_ID)).thenReturn(true);
    }

    private void givenItemNotOwned() {
        when(userStoreItemRepository.isOwned(USER_ID, ITEM_ID)).thenReturn(false);
    }

    private void givenInventory(List<UserStoreItem> inventory) {
        when(userStoreItemRepository.findAllByUserId(USER_ID)).thenReturn(inventory);
    }

    private StoreItem item(Long id, ItemCategory category) {
        return StoreItem.builder()
                .id(id).name("Item " + id).description("desc")
                .category(category).assetKey("asset-" + id).priceCoins(10).build();
    }

    private UserStoreItem userItem(Long itemId, ItemCategory category, boolean equipped) {
        return UserStoreItem.builder()
                .storeItem(item(itemId, category)).equipped(equipped).build();
    }

    // --- act ---

    private EquipStoreItemResponse equip() {
        return equipStoreItemUseCase.execute(new EquipStoreItemRequest(USER_ID, ITEM_ID));
    }

    // --- assert ---

    private void thenEquipThrowsNotFound() {
        assertThatThrownBy(this::equip).isInstanceOf(NotFoundException.class);
    }

    private void thenEquipThrowsBusinessRule() {
        assertThatThrownBy(this::equip).isInstanceOf(BusinessRuleException.class);
    }

    private void thenTargetEquipped(EquipStoreItemResponse result) {
        assertThat(result.equipped()).isTrue();
        verify(userStoreItemRepository).updateEquipped(USER_ID, ITEM_ID, true);
    }

    private void thenOtherHouseUnequippedAndTreeUntouched() {
        verify(userStoreItemRepository).updateEquipped(USER_ID, 20L, false);
        verify(userStoreItemRepository, never()).updateEquipped(USER_ID, 30L, false);
    }

    private void thenTargetWasNotUnequipped() {
        verify(userStoreItemRepository, never()).updateEquipped(USER_ID, ITEM_ID, false);
    }

    private void thenTargetWasNotEquipped() {
        verify(userStoreItemRepository, never()).updateEquipped(USER_ID, ITEM_ID, true);
    }
}
