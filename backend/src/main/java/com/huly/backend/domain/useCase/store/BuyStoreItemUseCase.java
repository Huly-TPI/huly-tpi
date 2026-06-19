package com.huly.backend.domain.useCase.store;
import com.huly.backend.domain.exception.BusinessRuleException;
import com.huly.backend.domain.model.StoreItem;
import com.huly.backend.domain.model.UserStoreItem;
import com.huly.backend.domain.repository.StoreItemRepository;
import com.huly.backend.domain.repository.UserStoreItemRepository;
import com.huly.backend.domain.service.payment.CoinService;
import com.huly.backend.infrastructure.presentation.exception.NotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@RequiredArgsConstructor
public class BuyStoreItemUseCase {

    private final StoreItemRepository storeItemRepository;
    private final UserStoreItemRepository userStoreItemRepository;
    private final CoinService coinService;

    @Transactional
    public void execute(Long userId, Long storeItemId) {
        StoreItem item = storeItemRepository.findById(storeItemId).orElseThrow(() -> new NotFoundException("Item no encontrado " + storeItemId));
         if (userStoreItemRepository.isOwned(userId, storeItemId)) {
            throw new BusinessRuleException("Ya tenés este item");
        }

        coinService.debit(userId, item.getPriceCoins());
        userStoreItemRepository.save(UserStoreItem.builder().userId(userId)
        .storeItem(item)
        .equipped(false)
        .acquiredAt(Instant.now()).build());

    }
    
}
