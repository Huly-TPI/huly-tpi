package com.huly.backend.domain.useCase.store;
import com.huly.backend.domain.dto.store.DeleteStoreItemRequest;
import com.huly.backend.domain.repository.StoreItemRepository;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class DeleteStoreItemUseCase {

    private final StoreItemRepository storeItemRepository;

    public void execute(DeleteStoreItemRequest request) {
        storeItemRepository.deleteById(request.id());
    }
    
}
