package com.huly.backend.domain.useCase.store;
import com.huly.backend.domain.exception.BusinessRuleException;
import com.huly.backend.domain.repository.UserStoreItemRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
public class UnequipStoreItemUseCase {

    private final UserStoreItemRepository userStoreItemRepository;

    @Transactional
    public void execute(Long userId, Long storeItemId) {
        if(!userStoreItemRepository.isOwned(userId, storeItemId)) {
            throw new BusinessRuleException("No tenés este item");
        }
        userStoreItemRepository.updateEquipped(userId, storeItemId, false);
    }
    
}
