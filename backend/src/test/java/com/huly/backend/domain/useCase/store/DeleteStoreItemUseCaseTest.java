package com.huly.backend.domain.useCase.store;

import com.huly.backend.domain.dto.store.DeleteStoreItemRequest;
import com.huly.backend.domain.repository.StoreItemRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class DeleteStoreItemUseCaseTest {

    private static final Long ITEM_ID = 10L;

    @Mock
    private StoreItemRepository storeItemRepository;

    private DeleteStoreItemUseCase deleteStoreItemUseCase;

    @BeforeEach
    void setUp() {
        deleteStoreItemUseCase = new DeleteStoreItemUseCase(storeItemRepository);
    }

    @Test
    @DisplayName("Delega el borrado del item al repositorio")
    void executeShouldDelegateToRepository() {
        delete();

        thenItemDeleted();
    }

    // --- act ---

    private void delete() {
        deleteStoreItemUseCase.execute(new DeleteStoreItemRequest(ITEM_ID));
    }

    // --- assert ---

    private void thenItemDeleted() {
        verify(storeItemRepository).deleteById(ITEM_ID);
    }
}
