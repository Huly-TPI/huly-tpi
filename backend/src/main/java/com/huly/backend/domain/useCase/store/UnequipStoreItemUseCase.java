package com.huly.backend.domain.useCase.store;
import com.huly.backend.domain.dto.store.UnequipStoreItemRequest;
import com.huly.backend.domain.dto.store.UnequipStoreItemResponse;
import com.huly.backend.domain.exception.BusinessRuleException;
import com.huly.backend.domain.mapper.store.UnequipStoreItemMapper;
import com.huly.backend.domain.repository.UserStoreItemRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
public class UnequipStoreItemUseCase {

    private final UserStoreItemRepository userStoreItemRepository;
    private final UnequipStoreItemMapper mapper;

    @Transactional
    public UnequipStoreItemResponse execute(UnequipStoreItemRequest request) {
        Long userId = request.userId();
        Long storeItemId = request.storeItemId();
        if(!userStoreItemRepository.isOwned(userId, storeItemId)) {
            throw new BusinessRuleException("No tenés este item");
        }
        userStoreItemRepository.updateEquipped(userId, storeItemId, false);
        return mapper.toResponse(true);
    }

}
