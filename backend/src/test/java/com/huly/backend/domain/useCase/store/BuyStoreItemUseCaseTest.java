package com.huly.backend.domain.useCase.store;

import com.huly.backend.domain.dto.store.BuyStoreItemRequest;
import com.huly.backend.domain.mapper.store.BuyStoreItemMapper;
import com.huly.backend.domain.model.user.UserPlan;
import com.huly.backend.domain.exception.BusinessRuleException;
import com.huly.backend.domain.exception.InsufficientCoinsException;
import com.huly.backend.domain.model.shop.StoreItem;
import com.huly.backend.domain.model.user.UserStoreItem;
import com.huly.backend.domain.model.enums.ItemCategory;
import com.huly.backend.domain.repository.StoreItemRepository;
import com.huly.backend.domain.repository.UserStoreItemRepository;
import com.huly.backend.domain.repository.user.UserPlanRepository;
import com.huly.backend.domain.service.payment.CoinService;
import com.huly.backend.infrastructure.presentation.exception.NotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.time.Instant;

@ExtendWith(MockitoExtension.class)
class BuyStoreItemUseCaseTest {

    @Mock
    private StoreItemRepository storeItemRepository;
    @Mock
    private UserStoreItemRepository userStoreItemRepository;
    @Mock
    private CoinService coinService;
    @Mock
    private UserPlanRepository userPlanRepository;
    @Mock
    private UserPlan userPlan;

    private BuyStoreItemUseCase buyStoreItemUseCase;

    @BeforeEach
    void setUp() {
        buyStoreItemUseCase = new BuyStoreItemUseCase(
                storeItemRepository, userStoreItemRepository, coinService, userPlanRepository, new BuyStoreItemMapper());
    }

    private StoreItem item() {
        return StoreItem.builder()
                .id(10L).name("Casa rosa").description("Casa de color rosa")
                .category(ItemCategory.HOUSE).assetKey("casa-rosa").priceCoins(50).premiumOnly(false)
                .build();
    }

    private StoreItem premiumItem() {
        return StoreItem.builder()
                .id(20L).name("Árbol Sakura").description("Árbol de sakura")
                .category(ItemCategory.TREE).assetKey("tree-sakura").priceCoins(200)
                .premiumOnly(true)
                .build();
    }

    @Test
    void buy_shouldThrowNotFound_whenItemDoesNotExist() {
        when(storeItemRepository.findById(10L)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> buyStoreItemUseCase.execute(new BuyStoreItemRequest(1L, 10L)))
                .isInstanceOf(NotFoundException.class);

        verifyNoInteractions(coinService);
    }

    @Test
    void buy_shouldThrownBusinessRule_whenAlreadyOwned() {
        when(storeItemRepository.findById(10L)).thenReturn(Optional.of(item()));
        when(userStoreItemRepository.isOwned(1L, 10L)).thenReturn(true);
        assertThatThrownBy(() -> buyStoreItemUseCase.execute(new BuyStoreItemRequest(1L, 10L)))
                .isInstanceOf(BusinessRuleException.class);

        verifyNoInteractions(coinService);
        verify(userStoreItemRepository, never()).save(any());
    }

    @Test
    void buy_shouldThrowInsufficientCoins_whenNoFunds() {
        when(storeItemRepository.findById(10L)).thenReturn(Optional.of(item()));
        when(userStoreItemRepository.isOwned(1L, 10L)).thenReturn(false);
        doThrow(new InsufficientCoinsException("Saldo insuficiente"))
                .when(coinService).debit(1L, 50);
        assertThatThrownBy(() -> buyStoreItemUseCase.execute(new BuyStoreItemRequest(1L, 10L)))
                .isInstanceOf(InsufficientCoinsException.class);

        verify(userStoreItemRepository, never()).save(any());
    }

    @Test
    void buy_shouldDebitAndRegisterOwnership_whenValid() {
        when(storeItemRepository.findById(10L)).thenReturn(Optional.of(item()));
        when(userStoreItemRepository.isOwned(1L, 10L)).thenReturn(false);
        buyStoreItemUseCase.execute(new BuyStoreItemRequest(1L, 10L));

        verify(userStoreItemRepository).save(any(UserStoreItem.class));
        verify(coinService).debit(1L, 50);
    }

    @Test
    void buy_shouldThrowBusinessRule_whenPremiumItemAndUserHasNoPlan() {
        when(storeItemRepository.findById(20L)).thenReturn(Optional.of(premiumItem()));
        when(userStoreItemRepository.isOwned(1L, 20L)).thenReturn(false);
        when(userPlanRepository.findByUser(1L)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> buyStoreItemUseCase.execute(new BuyStoreItemRequest(1L, 20L)))
                .isInstanceOf(BusinessRuleException.class);
        verifyNoInteractions(coinService);
    }

    @Test
    void buy_shouldThrowBusinessRule_whenPremiumItemAndUserHasBasicPlan() {
        when(storeItemRepository.findById(20L)).thenReturn(Optional.of(premiumItem()));
        when(userStoreItemRepository.isOwned(1L, 20L)).thenReturn(false);
        when(userPlanRepository.findByUser(1L)).thenReturn(Optional.of(userPlan));
        when(userPlan.isActive(any(Instant.class))).thenReturn(true);
        when(userPlan.getPlanCode()).thenReturn("BASIC");
        assertThatThrownBy(() -> buyStoreItemUseCase.execute(new BuyStoreItemRequest(1L, 20L)))
                .isInstanceOf(BusinessRuleException.class);
        verifyNoInteractions(coinService);
    }

    @Test
    void buy_shouldThrowBusinessRule_whenPremiumItemAndPlanExpired() {
        when(storeItemRepository.findById(20L)).thenReturn(Optional.of(premiumItem()));
        when(userStoreItemRepository.isOwned(1L, 20L)).thenReturn(false);
        when(userPlanRepository.findByUser(1L)).thenReturn(Optional.of(userPlan));
        when(userPlan.isActive(any(Instant.class))).thenReturn(false);
        assertThatThrownBy(() -> buyStoreItemUseCase.execute(new BuyStoreItemRequest(1L, 20L)))
                .isInstanceOf(BusinessRuleException.class);
        verifyNoInteractions(coinService);
    }

    @Test
    void buy_shouldDebitAndSave_whenPremiumItemAndUserHasPremiumPlan() {
        when(storeItemRepository.findById(20L)).thenReturn(Optional.of(premiumItem()));
        when(userStoreItemRepository.isOwned(1L, 20L)).thenReturn(false);
        when(userPlanRepository.findByUser(1L)).thenReturn(Optional.of(userPlan));
        when(userPlan.isActive(any(Instant.class))).thenReturn(true);
        when(userPlan.getPlanCode()).thenReturn("PREMIUM");
        buyStoreItemUseCase.execute(new BuyStoreItemRequest(1L, 20L));
        verify(coinService).debit(1L, 200);
        verify(userStoreItemRepository).save(any(UserStoreItem.class));
    }

}
