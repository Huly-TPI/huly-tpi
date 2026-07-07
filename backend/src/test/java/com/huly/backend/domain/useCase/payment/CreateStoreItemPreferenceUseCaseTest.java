package com.huly.backend.domain.useCase.payment;

import com.huly.backend.domain.exception.BusinessRuleException;
import com.huly.backend.domain.exception.ResourceNotFoundException;
import com.huly.backend.domain.model.enums.ItemCategory;
import com.huly.backend.domain.model.enums.PaymentStatus;
import com.huly.backend.domain.model.enums.ProductType;
import com.huly.backend.domain.model.payment.PaymentEvent;
import com.huly.backend.domain.model.payment.PaymentPreferenceResult;
import com.huly.backend.domain.model.shop.StoreItem;
import com.huly.backend.domain.port.MercadoPagoPort;
import com.huly.backend.domain.repository.StoreItemRepository;
import com.huly.backend.domain.repository.UserStoreItemRepository;
import com.huly.backend.domain.repository.payment.PaymentEventRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CreateStoreItemPreferenceUseCaseTest {

    private static final Long STORE_ITEM_ID = 10L;
    private static final Long NO_PRICE_ITEM_ID = 11L;
    private static final Long MISSING_ITEM_ID = 99L;
    private static final Long USER_ID = 5L;
    private static final String PREF_ID = "pref-123";
    private static final String INIT_POINT = "https://mp.com/checkout";
    private static final String UUID_REGEX =
            "^[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}$";

    @Mock
    private StoreItemRepository storeItemRepository;
    @Mock
    private MercadoPagoPort mercadoPagoPort;
    @Mock
    private PaymentEventRepository paymentEventRepository;
    @Mock
    private UserStoreItemRepository userStoreItemRepository;

    @InjectMocks
    private CreateStoreItemPreferenceUseCase useCase;

    @Test
    @DisplayName("Devuelve el id de preferencia y el init point cuando el item se compra con dinero")
    void executeShouldReturnPreferenceIdAndInitPointWhenItemBuyableWithMoney() {
        givenBuyableItemExists();
        givenItemNotOwned();
        givenPreferenceCreatedForUser();

        PaymentPreferenceResult result = createStoreItemPreference(STORE_ITEM_ID, USER_ID);

        thenReturnsPreference(result);
    }

    @Test
    @DisplayName("Guarda el evento como compra de store item, pendiente y con cero monedas")
    void executeShouldSaveEventAsStoreItemPurchasePendingWithZeroCoins() {
        givenBuyableItemExists();
        givenItemNotOwned();
        givenPreferenceCreatedWithId("pref-real-456");

        createStoreItemPreference(STORE_ITEM_ID, USER_ID);

        thenSavedEventIsStoreItemPurchase();
    }

    @Test
    @DisplayName("Pasa la referencia externa generada al puerto de Mercado Pago")
    void executeShouldPassExternalReferenceToMpPort() {
        givenBuyableItemExists();
        givenItemNotOwned();
        givenPreferenceCreatedWithId(PREF_ID);

        createStoreItemPreference(STORE_ITEM_ID, USER_ID);

        thenExternalReferencePassedToPort();
    }

    @Test
    @DisplayName("Lanza ResourceNotFound cuando el item no existe")
    void executeShouldThrowResourceNotFoundWhenItemDoesNotExist() {
        givenItemDoesNotExist();

        thenPreferenceCreationThrowsResourceNotFound(MISSING_ITEM_ID, USER_ID);
        thenNoPreferenceCreatedNorSaved();
    }

    @Test
    @DisplayName("Lanza error de negocio cuando el item no tiene precio en dinero")
    void executeShouldThrowBusinessRuleWhenItemHasNoMoneyPrice() {
        givenItemWithoutMoneyPrice();

        thenPreferenceCreationThrowsBusinessRule(NO_PRICE_ITEM_ID, USER_ID, "dinero");
        thenNoPreferenceCreatedNorSaved();
    }

    @Test
    @DisplayName("Lanza error de negocio cuando el usuario ya posee el item")
    void executeShouldThrowBusinessRuleWhenAlreadyOwned() {
        givenBuyableItemExists();
        givenItemAlreadyOwned();

        thenPreferenceCreationThrowsBusinessRule(STORE_ITEM_ID, USER_ID, "Ya tenés");
        thenNoPreferenceCreatedNorSaved();
    }

    // --- arrange ---

    private void givenBuyableItemExists() {
        when(storeItemRepository.findById(STORE_ITEM_ID)).thenReturn(Optional.of(itemWithPrice()));
    }

    private void givenItemWithoutMoneyPrice() {
        when(storeItemRepository.findById(NO_PRICE_ITEM_ID)).thenReturn(Optional.of(itemWithoutPrice()));
    }

    private void givenItemDoesNotExist() {
        when(storeItemRepository.findById(MISSING_ITEM_ID)).thenReturn(Optional.empty());
    }

    private void givenItemNotOwned() {
        when(userStoreItemRepository.isOwned(USER_ID, STORE_ITEM_ID)).thenReturn(false);
    }

    private void givenItemAlreadyOwned() {
        when(userStoreItemRepository.isOwned(USER_ID, STORE_ITEM_ID)).thenReturn(true);
    }

    private void givenPreferenceCreatedForUser() {
        when(mercadoPagoPort.createPreference(any(), eq(USER_ID), any()))
                .thenReturn(new PaymentPreferenceResult(PREF_ID, INIT_POINT));
        when(paymentEventRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
    }

    private void givenPreferenceCreatedWithId(String preferenceId) {
        when(mercadoPagoPort.createPreference(any(), any(), any()))
                .thenReturn(new PaymentPreferenceResult(preferenceId, INIT_POINT));
        when(paymentEventRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
    }

    private StoreItem itemWithPrice() {
        return StoreItem.builder()
                .id(STORE_ITEM_ID).name("Cuaderno rosa").description("Un cuaderno rosa")
                .category(ItemCategory.NOTEBOOK).assetKey("notebook-pink")
                .priceCoins(50).price(new BigDecimal("1000.00"))
                .build();
    }

    private StoreItem itemWithoutPrice() {
        return StoreItem.builder()
                .id(NO_PRICE_ITEM_ID).name("Cuaderno celeste").description("Un cuaderno celeste")
                .category(ItemCategory.NOTEBOOK).assetKey("notebook-blue")
                .priceCoins(50).price(null)
                .build();
    }

    // --- act ---

    private PaymentPreferenceResult createStoreItemPreference(Long storeItemId, Long userId) {
        return useCase.execute(storeItemId, userId);
    }

    // --- assert ---

    private void thenReturnsPreference(PaymentPreferenceResult result) {
        assertThat(result.getId()).isEqualTo(PREF_ID);
        assertThat(result.getInitPoint()).isEqualTo(INIT_POINT);
    }

    private void thenSavedEventIsStoreItemPurchase() {
        ArgumentCaptor<PaymentEvent> captor = ArgumentCaptor.forClass(PaymentEvent.class);
        verify(paymentEventRepository).save(captor.capture());
        PaymentEvent saved = captor.getValue();
        assertThat(saved.getUserId()).isEqualTo(USER_ID);
        assertThat(saved.getStoreItemId()).isEqualTo(STORE_ITEM_ID);
        assertThat(saved.getProductId()).isNull();
        assertThat(saved.getProductType()).isEqualTo(ProductType.STORE_ITEM);
        assertThat(saved.getStatus()).isEqualTo(PaymentStatus.PENDING);
        assertThat(saved.getCoinsAmount()).isEqualTo(0);
        assertThat(saved.getMpPreferenceId()).isEqualTo("pref-real-456");
        assertThat(saved.getExternalReference()).matches(UUID_REGEX);
        assertThat(saved.getCreatedAt()).isNotNull();
        assertThat(saved.getUpdatedAt()).isNotNull();
    }

    private void thenExternalReferencePassedToPort() {
        ArgumentCaptor<String> captor = ArgumentCaptor.forClass(String.class);
        verify(mercadoPagoPort).createPreference(any(), eq(USER_ID), captor.capture());
        assertThat(captor.getValue()).isNotBlank();
    }

    private void thenNoPreferenceCreatedNorSaved() {
        verify(mercadoPagoPort, never()).createPreference(any(), any(), any());
        verify(paymentEventRepository, never()).save(any());
    }

    private void thenPreferenceCreationThrowsResourceNotFound(Long storeItemId, Long userId) {
        assertThatThrownBy(() -> createStoreItemPreference(storeItemId, userId))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    private void thenPreferenceCreationThrowsBusinessRule(Long storeItemId, Long userId, String messageFragment) {
        assertThatThrownBy(() -> createStoreItemPreference(storeItemId, userId))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining(messageFragment);
    }
}
