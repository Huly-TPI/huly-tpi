package com.huly.backend.domain.useCase.store;
import com.huly.backend.domain.dto.store.DeleteStoreItemRequest;
import com.huly.backend.domain.repository.StoreItemRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.verify;
@ExtendWith(MockitoExtension.class)
class DeleteStoreItemUseCaseTest {

    @Mock
    private StoreItemRepository storeItemRepository;

    private DeleteStoreItemUseCase deleteStoreItemUseCase;

    @BeforeEach
    void setUp() {
        deleteStoreItemUseCase = new DeleteStoreItemUseCase(storeItemRepository);
    }

    @Test
    void execute_shouldDelegateToRepository() {
        deleteStoreItemUseCase.execute(new DeleteStoreItemRequest(10L));
        verify(storeItemRepository).deleteById(10L);
    }
    
}
