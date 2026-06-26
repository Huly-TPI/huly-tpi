package com.huly.backend.domain.useCase.store;
import com.huly.backend.domain.dto.store.UnequipStoreItemRequest;
import com.huly.backend.domain.exception.BusinessRuleException;
import com.huly.backend.domain.mapper.store.UnequipStoreItemMapper;
import com.huly.backend.domain.repository.UserStoreItemRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class UnequipStoreItemUseCaseTest {

    @Mock
    private UserStoreItemRepository userStoreItemRepository;

    private UnequipStoreItemUseCase unequipStoreItemUseCase;

    @BeforeEach
    void setUp() {
        unequipStoreItemUseCase = new UnequipStoreItemUseCase(userStoreItemRepository, new UnequipStoreItemMapper());
    }

    @Test
    void unequip_shouldThrowBusinessRule_whenNotOwned() {
        when(userStoreItemRepository.isOwned(1L, 10L)).thenReturn(false);
        assertThatThrownBy(() -> unequipStoreItemUseCase.execute(new UnequipStoreItemRequest(1L, 10L)))
                .isInstanceOf(BusinessRuleException.class);

        verify(userStoreItemRepository, never()).updateEquipped(1L, 10L, false);
    }

    @Test
    void unequip_shouldUnequip_whenValid() {
        when(userStoreItemRepository.isOwned(1L, 10L)).thenReturn(true);
        unequipStoreItemUseCase.execute(new UnequipStoreItemRequest(1L, 10L));
        verify(userStoreItemRepository).updateEquipped(1L, 10L, false);
    }
}
