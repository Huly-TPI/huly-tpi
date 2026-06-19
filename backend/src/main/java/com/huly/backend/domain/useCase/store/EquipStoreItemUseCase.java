package com.huly.backend.domain.useCase.store;
import com.huly.backend.domain.exception.BusinessRuleException;
import com.huly.backend.domain.model.StoreItem;
import com.huly.backend.domain.repository.StoreItemRepository;
import com.huly.backend.domain.repository.UserStoreItemRepository;
import com.huly.backend.infrastructure.presentation.exception.NotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
public class EquipStoreItemUseCase {


    private final StoreItemRepository storeItemRepository;
    private final UserStoreItemRepository userStoreItemRepository;

    @Transactional
    public void execute(Long userId, Long storeItemId) {
        StoreItem item = storeItemRepository.findById(storeItemId).orElseThrow(() -> new NotFoundException("Item no encontrado " + storeItemId));
        if (!userStoreItemRepository.isOwned(userId, storeItemId)) {
            throw new BusinessRuleException("No tenés este item");
        }
        userStoreItemRepository.findAllByUserId(userId).stream()
                .filter(owned -> owned.isEquipped() && owned.getStoreItem().getCategory() == item.getCategory()
                && !owned.getStoreItem().getId().equals(storeItemId))
                .forEach(owned -> userStoreItemRepository.updateEquipped(userId, owned.getStoreItem().getId(), false));

                userStoreItemRepository.updateEquipped(userId, storeItemId, true);
    }
    
}
