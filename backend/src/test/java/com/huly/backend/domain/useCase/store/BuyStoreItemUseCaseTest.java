package com.huly.backend.domain.useCase.store;

import com.huly.backend.domain.dto.store.BuyStoreItemRequest;
import com.huly.backend.domain.dto.store.BuyStoreItemResponse;
import com.huly.backend.domain.exception.BusinessRuleException;
import com.huly.backend.domain.exception.InsufficientCoinsException;
import com.huly.backend.domain.mapper.store.BuyStoreItemMapper;
import com.huly.backend.domain.model.enums.ItemCategory;
import com.huly.backend.domain.model.shop.StoreItem;
import com.huly.backend.domain.model.user.UserPlan;
import com.huly.backend.domain.model.user.UserStoreItem;
import com.huly.backend.domain.repository.StoreItemRepository;
import com.huly.backend.domain.repository.UserStoreItemRepository;
import com.huly.backend.domain.repository.user.UserPlanRepository;
import com.huly.backend.domain.service.payment.CoinService;
import com.huly.backend.infrastructure.presentation.exception.NotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BuyStoreItemUseCaseTest {

    private static final Long USER_ID = 1L;
    private static final Long ITEM_ID = 10L;
    private static final Long PREMIUM_ITEM_ID = 20L;

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

    @Test
    @DisplayName("Lanza NotFound y no cobra cuando el item no existe")
    void buyShouldThrowNotFoundWhenItemDoesNotExist() {
        givenItemDoesNotExist();

        thenBuyThrowsNotFound();
        thenNoCoinsCharged();
    }

    @Test
    @DisplayName("Lanza error de negocio y no cobra cuando el item ya fue comprado")
    void buyShouldThrowBusinessRuleWhenAlreadyOwned() {
        givenItemExists();
        givenItemAlreadyOwned();

        thenBuyThrowsBusinessRule();
        thenNoCoinsCharged();
        thenNoOwnershipSaved();
    }

    @Test
    @DisplayName("Lanza monedas insuficientes y no registra la compra cuando no hay saldo")
    void buyShouldThrowInsufficientCoinsWhenNoFunds() {
        givenItemExists();
        givenItemNotOwned();
        givenInsufficientFunds();

        thenBuyThrowsInsufficientCoins();
        thenNoOwnershipSaved();
    }

    @Test
    @DisplayName("Debita las monedas y registra la propiedad cuando la compra es válida")
    void buyShouldDebitAndRegisterOwnershipWhenValid() {
        givenItemExists();
        givenItemNotOwned();

        BuyStoreItemResponse result = buy();

        thenPurchaseCompleted(result, 50);
    }

    @Test
    @DisplayName("Lanza error de negocio cuando el item es premium y el usuario no tiene plan")
    void buyShouldThrowBusinessRuleWhenPremiumItemAndUserHasNoPlan() {
        givenPremiumItemExists();
        givenPremiumItemNotOwned();
        givenUserHasNoPlan();

        thenBuyPremiumThrowsBusinessRule();
        thenNoCoinsCharged();
    }

    @Test
    @DisplayName("Lanza error de negocio cuando el item es premium y el usuario tiene plan básico")
    void buyShouldThrowBusinessRuleWhenPremiumItemAndUserHasBasicPlan() {
        givenPremiumItemExists();
        givenPremiumItemNotOwned();
        givenUserHasActiveBasicPlan();

        thenBuyPremiumThrowsBusinessRule();
        thenNoCoinsCharged();
    }

    @Test
    @DisplayName("Lanza error de negocio cuando el item es premium y el plan está vencido")
    void buyShouldThrowBusinessRuleWhenPremiumItemAndPlanExpired() {
        givenPremiumItemExists();
        givenPremiumItemNotOwned();
        givenUserPlanExpired();

        thenBuyPremiumThrowsBusinessRule();
        thenNoCoinsCharged();
    }

    @Test
    @DisplayName("Debita y guarda cuando el item es premium y el usuario tiene plan Premium activo")
    void buyShouldDebitAndSaveWhenPremiumItemAndUserHasPremiumPlan() {
        givenPremiumItemExists();
        givenPremiumItemNotOwned();
        givenUserHasActivePremiumPlan();

        BuyStoreItemResponse result = buyPremium();

        thenPurchaseCompleted(result, 200);
    }

    // --- arrange ---

    private void givenItemDoesNotExist() {
        when(storeItemRepository.findById(ITEM_ID)).thenReturn(Optional.empty());
    }

    private void givenItemExists() {
        when(storeItemRepository.findById(ITEM_ID)).thenReturn(Optional.of(item()));
    }

    private void givenPremiumItemExists() {
        when(storeItemRepository.findById(PREMIUM_ITEM_ID)).thenReturn(Optional.of(premiumItem()));
    }

    private void givenItemAlreadyOwned() {
        when(userStoreItemRepository.isOwned(USER_ID, ITEM_ID)).thenReturn(true);
    }

    private void givenItemNotOwned() {
        when(userStoreItemRepository.isOwned(USER_ID, ITEM_ID)).thenReturn(false);
    }

    private void givenPremiumItemNotOwned() {
        when(userStoreItemRepository.isOwned(USER_ID, PREMIUM_ITEM_ID)).thenReturn(false);
    }

    private void givenInsufficientFunds() {
        doThrow(new InsufficientCoinsException("Saldo insuficiente")).when(coinService).debit(USER_ID, 50);
    }

    private void givenUserHasNoPlan() {
        when(userPlanRepository.findByUser(USER_ID)).thenReturn(Optional.empty());
    }

    private void givenUserHasActiveBasicPlan() {
        when(userPlanRepository.findByUser(USER_ID)).thenReturn(Optional.of(userPlan));
        when(userPlan.isActive(any(Instant.class))).thenReturn(true);
        when(userPlan.getPlanCode()).thenReturn("BASIC");
    }

    private void givenUserPlanExpired() {
        when(userPlanRepository.findByUser(USER_ID)).thenReturn(Optional.of(userPlan));
        when(userPlan.isActive(any(Instant.class))).thenReturn(false);
    }

    private void givenUserHasActivePremiumPlan() {
        when(userPlanRepository.findByUser(USER_ID)).thenReturn(Optional.of(userPlan));
        when(userPlan.isActive(any(Instant.class))).thenReturn(true);
        when(userPlan.getPlanCode()).thenReturn("PREMIUM");
    }

    private StoreItem item() {
        return StoreItem.builder()
                .id(ITEM_ID).name("Casa rosa").description("Casa de color rosa")
                .category(ItemCategory.HOUSE).assetKey("casa-rosa").priceCoins(50).premiumOnly(false)
                .build();
    }

    private StoreItem premiumItem() {
        return StoreItem.builder()
                .id(PREMIUM_ITEM_ID).name("Árbol Sakura").description("Árbol de sakura")
                .category(ItemCategory.TREE).assetKey("tree-sakura").priceCoins(200).premiumOnly(true)
                .build();
    }

    // --- act ---

    private BuyStoreItemResponse buy() {
        return buyStoreItemUseCase.execute(new BuyStoreItemRequest(USER_ID, ITEM_ID));
    }

    private BuyStoreItemResponse buyPremium() {
        return buyStoreItemUseCase.execute(new BuyStoreItemRequest(USER_ID, PREMIUM_ITEM_ID));
    }

    // --- assert ---

    private void thenBuyThrowsNotFound() {
        assertThatThrownBy(this::buy).isInstanceOf(NotFoundException.class);
    }

    private void thenBuyThrowsBusinessRule() {
        assertThatThrownBy(this::buy).isInstanceOf(BusinessRuleException.class);
    }

    private void thenBuyThrowsInsufficientCoins() {
        assertThatThrownBy(this::buy).isInstanceOf(InsufficientCoinsException.class);
    }

    private void thenBuyPremiumThrowsBusinessRule() {
        assertThatThrownBy(this::buyPremium).isInstanceOf(BusinessRuleException.class);
    }

    private void thenPurchaseCompleted(BuyStoreItemResponse result, int coins) {
        assertThat(result.purchased()).isTrue();
        verify(coinService).debit(USER_ID, coins);
        verify(userStoreItemRepository).save(any(UserStoreItem.class));
    }

    private void thenNoCoinsCharged() {
        verifyNoInteractions(coinService);
    }

    private void thenNoOwnershipSaved() {
        verify(userStoreItemRepository, never()).save(any());
    }
}
