package com.huly.backend.domain.useCase.store;

import com.huly.backend.domain.dto.store.UnequipStoreItemRequest;
import com.huly.backend.domain.dto.store.UnequipStoreItemResponse;
import com.huly.backend.domain.exception.BusinessRuleException;
import com.huly.backend.domain.mapper.store.UnequipStoreItemMapper;
import com.huly.backend.domain.repository.UserStoreItemRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UnequipStoreItemUseCaseTest {

    private static final Long USER_ID = 1L;
    private static final Long ITEM_ID = 10L;

    @Mock
    private UserStoreItemRepository userStoreItemRepository;

    private UnequipStoreItemUseCase unequipStoreItemUseCase;

    @BeforeEach
    void setUp() {
        unequipStoreItemUseCase = new UnequipStoreItemUseCase(userStoreItemRepository, new UnequipStoreItemMapper());
    }

    @Test
    @DisplayName("Lanza error de negocio y no desequipa cuando el usuario no posee el item")
    void unequipShouldThrowBusinessRuleWhenNotOwned() {
        givenItemNotOwned();

        thenUnequipThrowsBusinessRule();
        thenItemWasNotUnequipped();
    }

    @Test
    @DisplayName("Desequipa el item cuando el usuario lo posee")
    void unequipShouldUnequipWhenValid() {
        givenItemOwned();

        UnequipStoreItemResponse result = unequip();

        thenItemUnequipped(result);
    }

    // --- arrange ---

    private void givenItemOwned() {
        when(userStoreItemRepository.isOwned(USER_ID, ITEM_ID)).thenReturn(true);
    }

    private void givenItemNotOwned() {
        when(userStoreItemRepository.isOwned(USER_ID, ITEM_ID)).thenReturn(false);
    }

    // --- act ---

    private UnequipStoreItemResponse unequip() {
        return unequipStoreItemUseCase.execute(new UnequipStoreItemRequest(USER_ID, ITEM_ID));
    }

    // --- assert ---

    private void thenUnequipThrowsBusinessRule() {
        assertThatThrownBy(this::unequip).isInstanceOf(BusinessRuleException.class);
    }

    private void thenItemUnequipped(UnequipStoreItemResponse result) {
        assertThat(result.unequipped()).isTrue();
        verify(userStoreItemRepository).updateEquipped(USER_ID, ITEM_ID, false);
    }

    private void thenItemWasNotUnequipped() {
        verify(userStoreItemRepository, never()).updateEquipped(USER_ID, ITEM_ID, false);
    }
}
