package com.huly.backend.domain.useCase.store;
import com.huly.backend.domain.model.shop.StoreItem;
import com.huly.backend.domain.repository.StoreItemRepository;
import lombok.RequiredArgsConstructor;

import java.util.List;

@RequiredArgsConstructor
public class ListStoreItemsUseCase {

    private final StoreItemRepository storeItemRepository;
    
    public List<StoreItem> execute() {
        return storeItemRepository.findAll();
    }
    
}
