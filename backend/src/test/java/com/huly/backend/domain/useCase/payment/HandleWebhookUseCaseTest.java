package com.huly.backend.domain.useCase.payment;

import com.huly.backend.domain.exception.ResourceNotFoundException;
import com.huly.backend.domain.model.enums.ItemCategory;
import com.huly.backend.domain.model.enums.PaymentStatus;
import com.huly.backend.domain.model.enums.ProductType;
import com.huly.backend.domain.model.payment.MercadoPagoPaymentResult;
import com.huly.backend.domain.model.payment.PaymentEvent;
import com.huly.backend.domain.model.shop.StoreItem;
import com.huly.backend.domain.model.user.UserStoreItem;
import com.huly.backend.domain.port.MercadoPagoPort;
import com.huly.backend.domain.repository.StoreItemRepository;
import com.huly.backend.domain.repository.UserStoreItemRepository;
import com.huly.backend.domain.repository.payment.PaymentEventRepository;
import com.huly.backend.domain.service.payment.CoinService;
import com.huly.backend.domain.service.payment.PlanService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class HandleWebhookUseCaseTest {

    private static final Long PAYMENT_ID = 99L;
    private static final Long EVENT_ID = 1L;
    private static final Long USER_ID = 10L;
    private static final Long PLAN_PRODUCT_ID = 7L;
    private static final Long STORE_ITEM_ID = 3L;
    private static final String EXT_REF = "uuid-ext-ref";

    @Mock
    private PaymentEventRepository paymentEventRepository;
    @Mock
    private MercadoPagoPort mercadoPagoPort;
    @Mock
    private CoinService coinService;
    @Mock
    private PlanService planService;
    @Mock
    private StoreItemRepository storeItemRepository;
    @Mock
    private UserStoreItemRepository userStoreItemRepository;

    @InjectMocks
    private HandleWebhookUseCase handleWebhookUseCase;

    private PaymentEvent pendingEvent;

    @BeforeEach
    void setUp() {
        pendingEvent = PaymentEvent.builder()
                .id(EVENT_ID)
                .userId(USER_ID)
                .productId(2L)
                .externalReference(EXT_REF)
                .mpPreferenceId("pref-123")
                .status(PaymentStatus.PENDING)
                .coinsAmount(500)
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();
    }

    @Test
    @DisplayName("Ignora el procesamiento cuando el evento ya está aprobado")
    void executeShouldSkipProcessingWhenEventAlreadyApproved() {
        givenPriorApprovedEvent();

        handleWebhook();

        thenMercadoPagoNotQueried();
        thenNoCoinsCredited();
    }

    @Test
    @DisplayName("Acredita monedas cuando el pago está aprobado y el evento se halla por referencia externa")
    void executeShouldCreditCoinsWhenPaymentApprovedAndEventFoundByExternalReference() {
        givenNoPriorEvent();
        givenMpPaymentApproved();
        givenEventFoundByExternalReference(pendingEvent);
        givenApprovalSucceeds();

        handleWebhook();

        thenCoinsCredited(500);
        thenPlanNotActivated();
    }

    @Test
    @DisplayName("Activa el plan y no acredita monedas cuando el evento aprobado es un plan sin bonus")
    void executeShouldActivatePlanAndNotCreditCoinsWhenApprovedEventIsPlan() {
        givenNoPriorEvent();
        givenMpPaymentApproved();
        givenEventFoundByExternalReference(planEvent(0));
        givenApprovalSucceeds();

        handleWebhook();

        thenPlanActivated(PLAN_PRODUCT_ID);
        thenNoCoinsCredited();
    }

    @Test
    @DisplayName("Activa el plan y acredita monedas cuando el plan aprobado incluye monedas bonus")
    void executeShouldActivatePlanAndCreditCoinsWhenApprovedPlanHasCoins() {
        givenNoPriorEvent();
        givenMpPaymentApproved();
        givenEventFoundByExternalReference(planEvent(300));
        givenApprovalSucceeds();

        handleWebhook();

        thenPlanActivated(PLAN_PRODUCT_ID);
        thenCoinsCredited(300);
    }

    @Test
    @DisplayName("Activa el plan sin acreditar monedas cuando el plan no define monedas bonus")
    void executeShouldActivatePlanWithoutCreditingWhenPlanCoinsAmountIsNull() {
        givenNoPriorEvent();
        givenMpPaymentApproved();
        givenEventFoundByExternalReference(planEvent(null));
        givenApprovalSucceeds();

        handleWebhook();

        thenPlanActivated(PLAN_PRODUCT_ID);
        thenNoCoinsCredited();
    }

    @Test
    @DisplayName("No activa el plan cuando la aprobación ya la hizo un webhook concurrente")
    void executeShouldNotActivatePlanWhenApproveIfPendingReturnsFalseForPlan() {
        givenNoPriorEvent();
        givenMpPaymentApproved();
        givenEventFoundByExternalReference(planEvent(0));
        givenApprovalAlreadyDone();

        handleWebhook();

        thenPlanNotActivated();
        thenNoCoinsCredited();
    }

    @Test
    @DisplayName("Acredita monedas cuando el evento se halla por id de pago como respaldo")
    void executeShouldCreditCoinsWhenPaymentApprovedAndEventFoundByPaymentIdAsFallback() {
        givenPriorPendingEvent();
        givenMpPaymentApproved();
        givenNoEventFoundByExternalReference(EXT_REF);
        givenApprovalSucceeds();

        handleWebhook();

        thenCoinsCredited(500);
    }

    @Test
    @DisplayName("No acredita monedas cuando la aprobación ya la hizo un webhook concurrente")
    void executeShouldNotCreditCoinsWhenApproveIfPendingReturnsFalse() {
        givenNoPriorEvent();
        givenMpPaymentApproved();
        givenEventFoundByExternalReference(pendingEvent);
        givenApprovalAlreadyDone();

        handleWebhook();

        thenNoCoinsCredited();
    }

    @Test
    @DisplayName("Marca el evento como fallido y no acredita monedas cuando el pago es rechazado")
    void executeShouldMarkEventFailedAndNotCreditCoinsWhenPaymentRejected() {
        givenNoPriorEvent();
        givenMpPaymentWithStatus("rejected", "cc_rejected_insufficient_amount");
        givenEventFoundByExternalReference(pendingEvent);

        handleWebhook();

        thenEventMarkedFailed();
        thenNoCoinsCredited();
    }

    @Test
    @DisplayName("Marca el evento como fallido cuando el pago es cancelado")
    void executeShouldMarkEventFailedWhenPaymentCancelled() {
        givenNoPriorEvent();
        givenMpPaymentWithStatus("cancelled", "expired");
        givenEventFoundByExternalReference(pendingEvent);

        handleWebhook();

        thenEventMarkedFailed();
        thenNoCoinsCredited();
    }

    @Test
    @DisplayName("No hace nada cuando el pago tiene un estado intermedio")
    void executeShouldDoNothingWhenPaymentHasIntermediateStatus() {
        givenNoPriorEvent();
        givenMpPaymentWithStatus("in_process", null);
        givenEventFoundByExternalReference(pendingEvent);

        handleWebhook();

        thenNoStatusTransition();
        thenNoCoinsCredited();
    }

    @Test
    @DisplayName("No hace nada cuando el estado del pago es pending")
    void executeShouldDoNothingWhenPaymentStatusIsPending() {
        givenNoPriorEvent();
        givenMpPaymentWithStatus("pending", null);
        givenEventFoundByExternalReference(pendingEvent);

        handleWebhook();

        thenNoStatusTransition();
        thenNoCoinsCredited();
    }

    @Test
    @DisplayName("No hace nada cuando no se encuentra ningún evento")
    void executeShouldDoNothingWhenNoEventFound() {
        givenNoPriorEvent();
        givenMpPaymentApprovedWithReference("unknown-ref");
        givenNoEventFoundByExternalReference("unknown-ref");

        handleWebhook();

        thenNoStatusTransition();
        thenNoCoinsCredited();
    }

    @Test
    @DisplayName("Otorga la posesión del store item y no acredita monedas cuando el evento aprobado es un store item")
    void executeShouldGrantStoreItemOwnershipAndNotCreditCoinsWhenApprovedEventIsStoreItem() {
        givenNoPriorEvent();
        givenMpPaymentApproved();
        givenEventFoundByExternalReference(storeItemEvent());
        givenApprovalSucceeds();
        givenStoreItemNotOwned();
        givenStoreItemExists();

        handleWebhook();

        thenStoreItemGranted();
        thenNoCoinsCredited();
        thenPlanNotActivated();
    }

    @Test
    @DisplayName("No otorga el store item dos veces cuando el usuario ya lo posee")
    void executeShouldNotGrantStoreItemTwiceWhenAlreadyOwned() {
        givenNoPriorEvent();
        givenMpPaymentApproved();
        givenEventFoundByExternalReference(storeItemEvent());
        givenApprovalSucceeds();
        givenStoreItemAlreadyOwned();

        handleWebhook();

        thenStoreItemNotGranted();
        thenStoreItemNotLookedUp();
        thenNoCoinsCredited();
    }

    @Test
    @DisplayName("No otorga el store item cuando la aprobación ya la hizo un webhook concurrente")
    void executeShouldNotGrantStoreItemWhenApproveIfPendingReturnsFalse() {
        givenNoPriorEvent();
        givenMpPaymentApproved();
        givenEventFoundByExternalReference(storeItemEvent());
        givenApprovalAlreadyDone();

        handleWebhook();

        thenStoreItemNotGranted();
        thenNoCoinsCredited();
    }

    @Test
    @DisplayName("Lanza ResourceNotFound cuando el store item no existe al otorgarlo")
    void executeShouldThrowResourceNotFoundWhenStoreItemMissingOnGrant() {
        givenNoPriorEvent();
        givenMpPaymentApproved();
        givenEventFoundByExternalReference(storeItemEvent());
        givenApprovalSucceeds();
        givenStoreItemNotOwned();
        givenStoreItemMissing();

        thenHandlingWebhookThrowsResourceNotFound();
        thenStoreItemNotGranted();
    }

    // --- arrange ---

    private void givenNoPriorEvent() {
        when(paymentEventRepository.findByMpPaymentId(PAYMENT_ID)).thenReturn(Optional.empty());
    }

    private void givenPriorPendingEvent() {
        when(paymentEventRepository.findByMpPaymentId(PAYMENT_ID)).thenReturn(Optional.of(pendingEvent));
    }

    private void givenPriorApprovedEvent() {
        PaymentEvent approvedEvent = PaymentEvent.builder()
                .id(EVENT_ID).userId(USER_ID).status(PaymentStatus.APPROVED).coinsAmount(500)
                .createdAt(Instant.now()).updatedAt(Instant.now()).build();
        when(paymentEventRepository.findByMpPaymentId(PAYMENT_ID)).thenReturn(Optional.of(approvedEvent));
    }

    private void givenMpPaymentApproved() {
        when(mercadoPagoPort.getPayment(PAYMENT_ID))
                .thenReturn(new MercadoPagoPaymentResult(PAYMENT_ID, EXT_REF, "approved", "accredited"));
    }

    private void givenMpPaymentApprovedWithReference(String reference) {
        when(mercadoPagoPort.getPayment(PAYMENT_ID))
                .thenReturn(new MercadoPagoPaymentResult(PAYMENT_ID, reference, "approved", "accredited"));
    }

    private void givenMpPaymentWithStatus(String status, String detail) {
        when(mercadoPagoPort.getPayment(PAYMENT_ID))
                .thenReturn(new MercadoPagoPaymentResult(PAYMENT_ID, EXT_REF, status, detail));
    }

    private void givenEventFoundByExternalReference(PaymentEvent event) {
        when(paymentEventRepository.findByExternalReference(EXT_REF)).thenReturn(Optional.of(event));
    }

    private void givenNoEventFoundByExternalReference(String reference) {
        when(paymentEventRepository.findByExternalReference(reference)).thenReturn(Optional.empty());
    }

    private void givenApprovalSucceeds() {
        when(paymentEventRepository.approveIfPending(EVENT_ID, PAYMENT_ID)).thenReturn(true);
    }

    private void givenApprovalAlreadyDone() {
        when(paymentEventRepository.approveIfPending(EVENT_ID, PAYMENT_ID)).thenReturn(false);
    }

    private void givenStoreItemNotOwned() {
        when(userStoreItemRepository.isOwned(USER_ID, STORE_ITEM_ID)).thenReturn(false);
    }

    private void givenStoreItemAlreadyOwned() {
        when(userStoreItemRepository.isOwned(USER_ID, STORE_ITEM_ID)).thenReturn(true);
    }

    private void givenStoreItemExists() {
        when(storeItemRepository.findById(STORE_ITEM_ID)).thenReturn(Optional.of(storeItem()));
    }

    private void givenStoreItemMissing() {
        when(storeItemRepository.findById(STORE_ITEM_ID)).thenReturn(Optional.empty());
    }

    private PaymentEvent planEvent(Integer coins) {
        return PaymentEvent.builder()
                .id(EVENT_ID).userId(USER_ID).productId(PLAN_PRODUCT_ID).externalReference(EXT_REF)
                .status(PaymentStatus.PENDING).coinsAmount(coins).productType(ProductType.PLAN)
                .createdAt(Instant.now()).updatedAt(Instant.now()).build();
    }

    private PaymentEvent storeItemEvent() {
        return PaymentEvent.builder()
                .id(EVENT_ID).userId(USER_ID).storeItemId(STORE_ITEM_ID).externalReference(EXT_REF)
                .status(PaymentStatus.PENDING).coinsAmount(0).productType(ProductType.STORE_ITEM)
                .createdAt(Instant.now()).updatedAt(Instant.now()).build();
    }

    private StoreItem storeItem() {
        return StoreItem.builder()
                .id(STORE_ITEM_ID).name("Cuaderno rosa").description("Un cuaderno rosa")
                .category(ItemCategory.NOTEBOOK).assetKey("notebook-pink")
                .priceCoins(50).price(new BigDecimal("1000.00")).build();
    }

    // --- act ---

    private void handleWebhook() {
        handleWebhookUseCase.execute(PAYMENT_ID);
    }

    // --- assert ---

    private void thenCoinsCredited(int coins) {
        verify(coinService).credit(USER_ID, coins);
    }

    private void thenNoCoinsCredited() {
        verifyNoInteractions(coinService);
    }

    private void thenPlanActivated(Long productId) {
        verify(planService).activate(USER_ID, productId);
    }

    private void thenPlanNotActivated() {
        verifyNoInteractions(planService);
    }

    private void thenMercadoPagoNotQueried() {
        verifyNoInteractions(mercadoPagoPort);
    }

    private void thenEventMarkedFailed() {
        verify(paymentEventRepository).updateStatus(eq(EVENT_ID), eq(PaymentStatus.FAILED), eq(PAYMENT_ID), any());
    }

    private void thenNoStatusTransition() {
        verify(paymentEventRepository, never()).approveIfPending(any(), any());
        verify(paymentEventRepository, never()).updateStatus(any(), any(), any(), any());
    }

    private void thenStoreItemGranted() {
        verify(userStoreItemRepository).save(any(UserStoreItem.class));
    }

    private void thenStoreItemNotGranted() {
        verify(userStoreItemRepository, never()).save(any());
    }

    private void thenStoreItemNotLookedUp() {
        verify(storeItemRepository, never()).findById(any());
    }

    private void thenHandlingWebhookThrowsResourceNotFound() {
        assertThatThrownBy(this::handleWebhook).isInstanceOf(ResourceNotFoundException.class);
    }
}
