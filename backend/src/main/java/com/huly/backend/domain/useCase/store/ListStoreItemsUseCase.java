package com.huly.backend.domain.useCase.store;
import com.huly.backend.domain.dto.store.ListStoreItemsResponse;
import com.huly.backend.domain.mapper.store.ListStoreItemsMapper;
import com.huly.backend.domain.repository.StoreItemRepository;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class ListStoreItemsUseCase {

    private final StoreItemRepository storeItemRepository;
    private final ListStoreItemsMapper mapper;

    public ListStoreItemsResponse execute() {
        return mapper.toResponse(storeItemRepository.findAll());
    }

}
