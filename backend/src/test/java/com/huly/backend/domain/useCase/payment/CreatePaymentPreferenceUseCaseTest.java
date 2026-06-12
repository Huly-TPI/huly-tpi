package com.huly.backend.domain.useCase.payment;

import com.huly.backend.domain.dto.payment.PaymentEvent;
import com.huly.backend.domain.dto.payment.PaymentPreferenceResult;
import com.huly.backend.domain.dto.payment.Product;
import com.huly.backend.domain.dto.payment.UserPlan;
import com.huly.backend.domain.exception.BusinessRuleException;
import com.huly.backend.domain.exception.ResourceNotFoundException;
import com.huly.backend.domain.model.enums.PaymentStatus;
import com.huly.backend.domain.model.enums.ProductType;
import com.huly.backend.domain.port.MercadoPagoPort;
import com.huly.backend.domain.repository.PaymentEventRepository;
import com.huly.backend.domain.repository.ProductRepository;
import com.huly.backend.domain.repository.UserPlanRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
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

    @Mock private ProductRepository productRepository;
    @Mock private MercadoPagoPort mercadoPagoPort;
    @Mock private PaymentEventRepository paymentEventRepository;
    @Mock private UserPlanRepository userPlanRepository;

    private CreatePaymentPreferenceUseCase useCase;

    private Product product;

    @BeforeEach
    void setUp() {
        useCase = new CreatePaymentPreferenceUseCase(
                productRepository, mercadoPagoPort, paymentEventRepository, userPlanRepository);

        product = Product.builder()
                .id(1L)
                .name("Pack Estándar")
                .description("500 monedas")
                .price(new BigDecimal("9.99"))
                .coinsAmount(500)
                .build();
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
                .id(1L).userId(10L).productId(productId).planCode("PREMIUM")
                .grantedAt(Instant.now().minus(1, ChronoUnit.DAYS))
                .expiresAt(Instant.now().plus(30, ChronoUnit.DAYS))
                .build();
    }

    @Test
    void execute_shouldReturnPreferenceIdAndInitPoint_whenProductExists() {
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        when(mercadoPagoPort.createPreference(eq(product), eq(10L), any()))
                .thenReturn(new PaymentPreferenceResult("pref-123", "https://mp.com/checkout"));
        when(paymentEventRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        PaymentPreferenceResult result = useCase.execute(1L, 10L);

        assertThat(result.getId()).isEqualTo("pref-123");
        assertThat(result.getInitPoint()).isEqualTo("https://mp.com/checkout");
    }

    @Test
    void execute_shouldSaveEventWithPendingStatusAndCorrectCoinsAmount() {
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        when(mercadoPagoPort.createPreference(any(), any(), any()))
                .thenReturn(new PaymentPreferenceResult("pref-123", "https://mp.com/checkout"));
        when(paymentEventRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        useCase.execute(1L, 10L);

        ArgumentCaptor<PaymentEvent> captor = ArgumentCaptor.forClass(PaymentEvent.class);
        verify(paymentEventRepository).save(captor.capture());
        PaymentEvent saved = captor.getValue();

        assertThat(saved.getUserId()).isEqualTo(10L);
        assertThat(saved.getProductId()).isEqualTo(1L);
        assertThat(saved.getStatus()).isEqualTo(PaymentStatus.PENDING);
        assertThat(saved.getCoinsAmount()).isEqualTo(500);
        assertThat(saved.getCreatedAt()).isNotNull();
        assertThat(saved.getUpdatedAt()).isNotNull();
    }

    @Test
    void execute_shouldSaveBothExternalReferenceAndRealMpPreferenceId() {
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        when(mercadoPagoPort.createPreference(any(), any(), any()))
                .thenReturn(new PaymentPreferenceResult("pref-real-456", "https://mp.com/checkout"));
        when(paymentEventRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        useCase.execute(1L, 10L);

        ArgumentCaptor<PaymentEvent> captor = ArgumentCaptor.forClass(PaymentEvent.class);
        verify(paymentEventRepository).save(captor.capture());
        PaymentEvent saved = captor.getValue();

        // externalReference es un UUID generado internamente (usado por el webhook para lookup)
        assertThat(saved.getExternalReference()).isNotNull();
        assertThat(saved.getExternalReference()).matches(
                "^[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}$");

        // mpPreferenceId es el ID real devuelto por MP (para auditoría/reconciliación)
        assertThat(saved.getMpPreferenceId()).isEqualTo("pref-real-456");
    }

    @Test
    void execute_shouldPassExternalReferenceToMpPort() {
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        when(mercadoPagoPort.createPreference(any(), any(), any()))
                .thenReturn(new PaymentPreferenceResult("pref-123", "https://mp.com/checkout"));
        when(paymentEventRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        useCase.execute(1L, 10L);

        ArgumentCaptor<String> externalRefCaptor = ArgumentCaptor.forClass(String.class);
        verify(mercadoPagoPort).createPreference(eq(product), eq(10L), externalRefCaptor.capture());
        assertThat(externalRefCaptor.getValue()).isNotBlank();
    }

    @Test
    void execute_shouldThrowResourceNotFoundException_whenProductNotFound() {
        when(productRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> useCase.execute(99L, 10L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void execute_shouldThrowBusinessRule_whenBuyingDifferentPlanWhileActive() {
        Product pro = planProduct(7L, "PRO");
        when(productRepository.findById(7L)).thenReturn(Optional.of(pro));
        when(userPlanRepository.findByUser(10L)).thenReturn(Optional.of(activePlan(5L)));

        assertThatThrownBy(() -> useCase.execute(7L, 10L))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("plan activo");

        verify(mercadoPagoPort, never()).createPreference(any(), any(), any());
        verify(paymentEventRepository, never()).save(any());
    }

    @Test
    void execute_shouldAllowRenewal_whenBuyingSamePlanWhileActive() {
        Product premium = planProduct(5L, "PREMIUM");
        when(productRepository.findById(5L)).thenReturn(Optional.of(premium));
        when(userPlanRepository.findByUser(10L)).thenReturn(Optional.of(activePlan(5L)));
        when(mercadoPagoPort.createPreference(eq(premium), eq(10L), any()))
                .thenReturn(new PaymentPreferenceResult("pref-123", "https://mp.com/checkout"));
        when(paymentEventRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        PaymentPreferenceResult result = useCase.execute(5L, 10L);

        assertThat(result.getId()).isEqualTo("pref-123");
        verify(paymentEventRepository).save(any());
    }

    @Test
    void execute_shouldAllowPlanPurchase_whenNoActiveMembership() {
        Product premium = planProduct(5L, "PREMIUM");
        when(productRepository.findById(5L)).thenReturn(Optional.of(premium));
        when(userPlanRepository.findByUser(10L)).thenReturn(Optional.empty());
        when(mercadoPagoPort.createPreference(any(), any(), any()))
                .thenReturn(new PaymentPreferenceResult("pref-123", "https://mp.com/checkout"));
        when(paymentEventRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        PaymentPreferenceResult result = useCase.execute(5L, 10L);

        assertThat(result.getInitPoint()).isEqualTo("https://mp.com/checkout");
        verify(paymentEventRepository).save(any());
    }

    @Test
    void execute_shouldAllowPlanPurchase_whenMembershipExpired() {
        Product premium = planProduct(5L, "PREMIUM");
        UserPlan expired = UserPlan.builder()
                .id(1L).userId(10L).planCode("PREMIUM")
                .grantedAt(Instant.now().minus(60, ChronoUnit.DAYS))
                .expiresAt(Instant.now().minus(1, ChronoUnit.DAYS))
                .build();
        when(productRepository.findById(5L)).thenReturn(Optional.of(premium));
        when(userPlanRepository.findByUser(10L)).thenReturn(Optional.of(expired));
        when(mercadoPagoPort.createPreference(any(), any(), any()))
                .thenReturn(new PaymentPreferenceResult("pref-123", "https://mp.com/checkout"));
        when(paymentEventRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        PaymentPreferenceResult result = useCase.execute(5L, 10L);

        assertThat(result.getId()).isEqualTo("pref-123");
        verify(paymentEventRepository).save(any());
    }
}
