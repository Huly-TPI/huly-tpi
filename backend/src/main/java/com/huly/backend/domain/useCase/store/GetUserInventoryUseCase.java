package com.huly.backend.domain.useCase.store;
import com.huly.backend.domain.dto.store.GetUserInventoryRequest;
import com.huly.backend.domain.dto.store.GetUserInventoryResponse;
import com.huly.backend.domain.mapper.store.GetUserInventoryMapper;
import com.huly.backend.domain.repository.UserStoreItemRepository;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class GetUserInventoryUseCase {

    private final UserStoreItemRepository userStoreItemRepository;
    private final GetUserInventoryMapper mapper;

    public GetUserInventoryResponse execute(GetUserInventoryRequest request) {
        return mapper.toResponse(userStoreItemRepository.findAllByUserId(request.userId()));
    }

}
