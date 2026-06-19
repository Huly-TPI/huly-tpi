package com.huly.backend.domain.useCase.store;
import com.huly.backend.domain.model.UserStoreItem;
import com.huly.backend.domain.repository.UserStoreItemRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class GetUserInventoryUseCaseTest {
    

    @Mock
    private UserStoreItemRepository userStoreItemRepository;

    @InjectMocks
    private GetUserInventoryUseCase getUserInventoryUseCase;

    @Test 
    void execute_shouldReturnUserInventory() {
        UserStoreItem item = UserStoreItem.builder().id(1L).userId(7L).equipped(true).build();
        when(userStoreItemRepository.findAllByUserId(7L)).thenReturn(List.of(item));
        List<UserStoreItem> result = getUserInventoryUseCase.execute(7L);

        assertThat(result).containsExactly(item);
        verify(userStoreItemRepository).findAllByUserId(7L);
    }
}
