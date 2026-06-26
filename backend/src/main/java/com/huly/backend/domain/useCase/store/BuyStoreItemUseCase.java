package com.huly.backend.domain.useCase.store;

import com.huly.backend.domain.dto.store.BuyStoreItemRequest;
import com.huly.backend.domain.dto.store.BuyStoreItemResponse;
import com.huly.backend.domain.exception.BusinessRuleException;
import com.huly.backend.domain.mapper.store.BuyStoreItemMapper;
import com.huly.backend.domain.model.user.UserPlan;
import com.huly.backend.domain.model.shop.StoreItem;
import com.huly.backend.domain.model.user.UserStoreItem;
import com.huly.backend.domain.repository.StoreItemRepository;
import com.huly.backend.domain.repository.UserStoreItemRepository;
import com.huly.backend.domain.service.payment.CoinService;
import com.huly.backend.domain.repository.user.UserPlanRepository;
import com.huly.backend.infrastructure.presentation.exception.NotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@RequiredArgsConstructor
public class BuyStoreItemUseCase {

    private final StoreItemRepository storeItemRepository;
    private final UserStoreItemRepository userStoreItemRepository;
    private final CoinService coinService;
    private final UserPlanRepository userPlanRepository;
    private final BuyStoreItemMapper mapper;

    @Transactional
    public BuyStoreItemResponse execute(BuyStoreItemRequest request) {
        Long userId = request.userId();
        Long storeItemId = request.storeItemId();
        StoreItem item = storeItemRepository.findById(storeItemId)
                .orElseThrow(() -> new NotFoundException("Item no encontrado " + storeItemId));
        if (userStoreItemRepository.isOwned(userId, storeItemId)) {
            throw new BusinessRuleException("Ya tenés este item");
        }

        if (item.isPremiumOnly()) {
            UserPlan plan = userPlanRepository.findByUser(userId)
                    .orElseThrow(() -> new BusinessRuleException("Este item es exclusivo para plan Premium"));
            if (!plan.isActive(Instant.now()) || !"PREMIUM".equals(plan.getPlanCode())) {
                throw new BusinessRuleException("Este item es exclusivo para plan Premium");
            }
        }

        coinService.debit(userId, item.getPriceCoins());
        userStoreItemRepository.save(UserStoreItem.builder().userId(userId)
                .storeItem(item)
                .equipped(false)
                .acquiredAt(Instant.now()).build());

        return mapper.toResponse(true);
    }

}
