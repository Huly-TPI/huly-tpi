package com.huly.backend.domain.useCase.payment;

import com.huly.backend.domain.model.payment.PaymentEvent;
import com.huly.backend.domain.model.payment.PaymentPreferenceResult;
import com.huly.backend.domain.model.payment.Product;
import com.huly.backend.domain.model.user.UserPlan;
import com.huly.backend.domain.exception.BusinessRuleException;
import com.huly.backend.domain.exception.ResourceNotFoundException;
import com.huly.backend.domain.model.enums.PaymentStatus;
import com.huly.backend.domain.model.enums.ProductType;
import com.huly.backend.domain.port.MercadoPagoPort;
import com.huly.backend.domain.repository.payment.PaymentEventRepository;
import com.huly.backend.domain.repository.payment.ProductRepository;
import com.huly.backend.domain.repository.user.UserPlanRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CreatePaymentPreferenceUseCaseTest {

    private static final Long COIN_PACK_ID = 1L;
    private static final Long PLAN_PRO_ID = 7L;
    private static final Long PLAN_PREMIUM_ID = 5L;
    private static final Long MISSING_ID = 99L;
    private static final Long USER_ID = 10L;
    private static final String PREF_ID = "pref-123";
    private static final String INIT_POINT = "https://mp.com/checkout";
    private static final String UUID_REGEX =
            "^[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}$";

    @Mock
    private ProductRepository productRepository;
    @Mock
    private MercadoPagoPort mercadoPagoPort;
    @Mock
    private PaymentEventRepository paymentEventRepository;
    @Mock
    private UserPlanRepository userPlanRepository;

    @InjectMocks
    private CreatePaymentPreferenceUseCase useCase;

    private Product coinPack;
    private Product planUnderTest;

    @BeforeEach
    void setUp() {
        coinPack = Product.builder()
                .id(COIN_PACK_ID)
                .name("Pack Estándar")
                .description("500 monedas")
                .price(new BigDecimal("9.99"))
                .coinsAmount(500)
                .build();
    }

    @Test
    @DisplayName("Devuelve el id de preferencia y el init point cuando el producto existe")
    void executeShouldReturnPreferenceIdAndInitPointWhenProductExists() {
        givenCoinPackExists();
        givenPreferenceCreatedForCoinPack();

        PaymentPreferenceResult result = createPreference(COIN_PACK_ID, USER_ID);

        thenReturnsPreference(result);
    }

    @Test
    @DisplayName("Guarda el evento en estado pendiente con la cantidad de monedas correcta")
    void executeShouldSaveEventWithPendingStatusAndCorrectCoinsAmount() {
        givenCoinPackExists();
        givenPreferenceCreatedWithId(PREF_ID);

        createPreference(COIN_PACK_ID, USER_ID);

        thenSavedEventIsPendingCoinPack();
    }

    @Test
    @DisplayName("Guarda tanto la referencia externa como el id real de preferencia de Mercado Pago")
    void executeShouldSaveBothExternalReferenceAndRealMpPreferenceId() {
        givenCoinPackExists();
        givenPreferenceCreatedWithId("pref-real-456");

        createPreference(COIN_PACK_ID, USER_ID);

        thenSavedEventHasGeneratedExternalReferenceAndRealPreferenceId();
    }

    @Test
    @DisplayName("Pasa la referencia externa generada al puerto de Mercado Pago")
    void executeShouldPassExternalReferenceToMpPort() {
        givenCoinPackExists();
        givenPreferenceCreatedWithId(PREF_ID);

        createPreference(COIN_PACK_ID, USER_ID);

        thenExternalReferencePassedToPort();
    }

    @Test
    @DisplayName("Lanza ResourceNotFound cuando el producto no existe")
    void executeShouldThrowResourceNotFoundExceptionWhenProductNotFound() {
        givenProductNotFound();

        thenCreatingPreferenceThrowsResourceNotFound(MISSING_ID, USER_ID);
    }

    @Test
    @DisplayName("Lanza error de negocio al comprar un plan distinto teniendo uno activo")
    void executeShouldThrowBusinessRuleWhenBuyingDifferentPlanWhileActive() {
        givenPlanProductExists(PLAN_PRO_ID, "PRO");
        givenActivePlan(PLAN_PREMIUM_ID);

        thenCreatingPreferenceThrowsBusinessRule(PLAN_PRO_ID, USER_ID, "plan activo");
        thenNoPreferenceCreatedNorSaved();
    }

    @Test
    @DisplayName("Permite renovar el mismo plan estando activo")
    void executeShouldAllowRenewalWhenBuyingSamePlanWhileActive() {
        givenPlanProductExists(PLAN_PREMIUM_ID, "PREMIUM");
        givenActivePlan(PLAN_PREMIUM_ID);
        givenPreferenceCreatedForPlan();

        PaymentPreferenceResult result = createPreference(PLAN_PREMIUM_ID, USER_ID);

        thenReturnsPreferenceId(result);
        thenEventSaved();
    }

    @Test
    @DisplayName("Permite comprar un plan cuando no hay membresía activa")
    void executeShouldAllowPlanPurchaseWhenNoActiveMembership() {
        givenPlanProductExists(PLAN_PREMIUM_ID, "PREMIUM");
        givenNoActivePlan();
        givenPreferenceCreatedWithId(PREF_ID);

        PaymentPreferenceResult result = createPreference(PLAN_PREMIUM_ID, USER_ID);

        thenReturnsInitPoint(result);
        thenEventSaved();
    }

    @Test
    @DisplayName("Permite comprar un plan cuando la membresía está vencida")
    void executeShouldAllowPlanPurchaseWhenMembershipExpired() {
        givenPlanProductExists(PLAN_PREMIUM_ID, "PREMIUM");
        givenExpiredPlan();
        givenPreferenceCreatedWithId(PREF_ID);

        PaymentPreferenceResult result = createPreference(PLAN_PREMIUM_ID, USER_ID);

        thenReturnsPreferenceId(result);
        thenEventSaved();
    }

    // --- arrange ---

    private void givenCoinPackExists() {
        when(productRepository.findById(COIN_PACK_ID)).thenReturn(Optional.of(coinPack));
    }

    private void givenProductNotFound() {
        when(productRepository.findById(MISSING_ID)).thenReturn(Optional.empty());
    }

    private void givenPlanProductExists(Long id, String planCode) {
        planUnderTest = planProduct(id, planCode);
        when(productRepository.findById(id)).thenReturn(Optional.of(planUnderTest));
    }

    private void givenActivePlan(Long productId) {
        when(userPlanRepository.findByUser(USER_ID)).thenReturn(Optional.of(activePlan(productId)));
    }

    private void givenNoActivePlan() {
        when(userPlanRepository.findByUser(USER_ID)).thenReturn(Optional.empty());
    }

    private void givenExpiredPlan() {
        when(userPlanRepository.findByUser(USER_ID)).thenReturn(Optional.of(expiredPlan()));
    }

    private void givenPreferenceCreatedForCoinPack() {
        when(mercadoPagoPort.createPreference(eq(coinPack), eq(USER_ID), any()))
                .thenReturn(new PaymentPreferenceResult(PREF_ID, INIT_POINT));
        when(paymentEventRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
    }

    private void givenPreferenceCreatedForPlan() {
        when(mercadoPagoPort.createPreference(eq(planUnderTest), eq(USER_ID), any()))
                .thenReturn(new PaymentPreferenceResult(PREF_ID, INIT_POINT));
        when(paymentEventRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
    }

    private void givenPreferenceCreatedWithId(String preferenceId) {
        when(mercadoPagoPort.createPreference(any(), any(), any()))
                .thenReturn(new PaymentPreferenceResult(preferenceId, INIT_POINT));
        when(paymentEventRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
    }

    private Product planProduct(Long id, String planCode) {
        return Product.builder()
                .id(id)
                .name("Plan " + planCode)
                .description("Suscripción")
                .price(new BigDecimal("9999"))
                .coinsAmount(0)
                .type(ProductType.PLAN)
                .planCode(planCode)
                .build();
    }

    private UserPlan activePlan(Long productId) {
        return UserPlan.builder()
                .id(1L).userId(USER_ID).productId(productId).planCode("PREMIUM")
                .grantedAt(Instant.now().minus(1, ChronoUnit.DAYS))
                .expiresAt(Instant.now().plus(30, ChronoUnit.DAYS))
                .build();
    }

    private UserPlan expiredPlan() {
        return UserPlan.builder()
                .id(1L).userId(USER_ID).planCode("PREMIUM")
                .grantedAt(Instant.now().minus(60, ChronoUnit.DAYS))
                .expiresAt(Instant.now().minus(1, ChronoUnit.DAYS))
                .build();
    }

    // --- act ---

    private PaymentPreferenceResult createPreference(Long productId, Long userId) {
        return useCase.execute(productId, userId);
    }

    // --- assert ---

    private void thenReturnsPreference(PaymentPreferenceResult result) {
        assertThat(result.getId()).isEqualTo(PREF_ID);
        assertThat(result.getInitPoint()).isEqualTo(INIT_POINT);
    }

    private void thenReturnsPreferenceId(PaymentPreferenceResult result) {
        assertThat(result.getId()).isEqualTo(PREF_ID);
    }

    private void thenReturnsInitPoint(PaymentPreferenceResult result) {
        assertThat(result.getInitPoint()).isEqualTo(INIT_POINT);
    }

    private void thenSavedEventIsPendingCoinPack() {
        ArgumentCaptor<PaymentEvent> captor = ArgumentCaptor.forClass(PaymentEvent.class);
        verify(paymentEventRepository).save(captor.capture());
        PaymentEvent saved = captor.getValue();
        assertThat(saved.getUserId()).isEqualTo(USER_ID);
        assertThat(saved.getProductId()).isEqualTo(COIN_PACK_ID);
        assertThat(saved.getStatus()).isEqualTo(PaymentStatus.PENDING);
        assertThat(saved.getCoinsAmount()).isEqualTo(500);
        assertThat(saved.getCreatedAt()).isNotNull();
        assertThat(saved.getUpdatedAt()).isNotNull();
    }

    private void thenSavedEventHasGeneratedExternalReferenceAndRealPreferenceId() {
        ArgumentCaptor<PaymentEvent> captor = ArgumentCaptor.forClass(PaymentEvent.class);
        verify(paymentEventRepository).save(captor.capture());
        PaymentEvent saved = captor.getValue();
        assertThat(saved.getExternalReference()).isNotNull();
        assertThat(saved.getExternalReference()).matches(UUID_REGEX);
        assertThat(saved.getMpPreferenceId()).isEqualTo("pref-real-456");
    }

    private void thenExternalReferencePassedToPort() {
        ArgumentCaptor<String> captor = ArgumentCaptor.forClass(String.class);
        verify(mercadoPagoPort).createPreference(eq(coinPack), eq(USER_ID), captor.capture());
        assertThat(captor.getValue()).isNotBlank();
    }

    private void thenEventSaved() {
        verify(paymentEventRepository).save(any());
    }

    private void thenNoPreferenceCreatedNorSaved() {
        verify(mercadoPagoPort, never()).createPreference(any(), any(), any());
        verify(paymentEventRepository, never()).save(any());
    }

    private void thenCreatingPreferenceThrowsResourceNotFound(Long productId, Long userId) {
        assertThatThrownBy(() -> createPreference(productId, userId))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    private void thenCreatingPreferenceThrowsBusinessRule(Long productId, Long userId, String messageFragment) {
        assertThatThrownBy(() -> createPreference(productId, userId))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining(messageFragment);
    }
}
