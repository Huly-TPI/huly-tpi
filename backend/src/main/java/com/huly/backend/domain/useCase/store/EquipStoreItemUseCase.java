package com.huly.backend.domain.useCase.store;
import com.huly.backend.domain.dto.store.EquipStoreItemRequest;
import com.huly.backend.domain.dto.store.EquipStoreItemResponse;
import com.huly.backend.domain.exception.BusinessRuleException;
import com.huly.backend.domain.mapper.store.EquipStoreItemMapper;
import com.huly.backend.domain.model.shop.StoreItem;
import com.huly.backend.domain.repository.StoreItemRepository;
import com.huly.backend.domain.repository.UserStoreItemRepository;
import com.huly.backend.infrastructure.presentation.exception.NotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
public class EquipStoreItemUseCase {


    private final StoreItemRepository storeItemRepository;
    private final UserStoreItemRepository userStoreItemRepository;
    private final EquipStoreItemMapper mapper;

    @Transactional
    public EquipStoreItemResponse execute(EquipStoreItemRequest request) {
        Long userId = request.userId();
        Long storeItemId = request.storeItemId();
        StoreItem item = storeItemRepository.findById(storeItemId).orElseThrow(() -> new NotFoundException("Item no encontrado " + storeItemId));
        if (!userStoreItemRepository.isOwned(userId, storeItemId)) {
            throw new BusinessRuleException("No tenés este item");
        }
        userStoreItemRepository.findAllByUserId(userId).stream()
                .filter(owned -> owned.isEquipped() && owned.getStoreItem().getCategory() == item.getCategory()
                && !owned.getStoreItem().getId().equals(storeItemId))
                .forEach(owned -> userStoreItemRepository.updateEquipped(userId, owned.getStoreItem().getId(), false));

                userStoreItemRepository.updateEquipped(userId, storeItemId, true);
        return mapper.toResponse(true);
    }

}
