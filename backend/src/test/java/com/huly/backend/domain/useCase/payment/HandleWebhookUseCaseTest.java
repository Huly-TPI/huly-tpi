package com.huly.backend.domain.useCase.payment;

import com.huly.backend.domain.model.enums.ItemCategory;
import com.huly.backend.domain.model.shop.StoreItem;
import com.huly.backend.domain.model.user.UserStoreItem;
import com.huly.backend.domain.repository.StoreItemRepository;
import com.huly.backend.domain.repository.UserStoreItemRepository;
import com.huly.backend.domain.model.payment.MercadoPagoPaymentResult;
import com.huly.backend.domain.model.payment.PaymentEvent;
import com.huly.backend.domain.model.enums.PaymentStatus;
import com.huly.backend.domain.model.enums.ProductType;
import com.huly.backend.domain.port.MercadoPagoPort;
import com.huly.backend.domain.repository.payment.PaymentEventRepository;
import com.huly.backend.domain.service.payment.CoinService;
import com.huly.backend.domain.service.payment.PlanService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.Optional;
import java.math.BigDecimal;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class HandleWebhookUseCaseTest {

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

        private PaymentEvent storeItemEvent() {
                return PaymentEvent.builder()
                                .id(1L).userId(10L).storeItemId(3L).externalReference("uuid-ext-ref")
                                .status(PaymentStatus.PENDING).coinsAmount(0).productType(ProductType.STORE_ITEM)
                                .createdAt(Instant.now()).updatedAt(Instant.now()).build();
        }

        @BeforeEach
        void setUp() {
                pendingEvent = PaymentEvent.builder()
                                .id(1L)
                                .userId(10L)
                                .productId(2L)
                                .externalReference("uuid-ext-ref")
                                .mpPreferenceId("pref-123")
                                .status(PaymentStatus.PENDING)
                                .coinsAmount(500)
                                .createdAt(Instant.now())
                                .updatedAt(Instant.now())
                                .build();
        }

        @Test
        void execute_shouldSkipProcessing_whenEventAlreadyApproved() {
                PaymentEvent approvedEvent = PaymentEvent.builder()
                                .id(1L).userId(10L).status(PaymentStatus.APPROVED).coinsAmount(500)
                                .createdAt(Instant.now()).updatedAt(Instant.now()).build();
                when(paymentEventRepository.findByMpPaymentId(99L)).thenReturn(Optional.of(approvedEvent));

                handleWebhookUseCase.execute(99L);

                verifyNoInteractions(mercadoPagoPort);
                verifyNoInteractions(coinService);
        }

        @Test
        void execute_shouldCreditCoins_whenPaymentApproved_andEventFoundByExternalReference() {
                when(paymentEventRepository.findByMpPaymentId(99L)).thenReturn(Optional.empty());
                when(mercadoPagoPort.getPayment(99L))
                                .thenReturn(new MercadoPagoPaymentResult(99L, "uuid-ext-ref", "approved",
                                                "accredited"));
                when(paymentEventRepository.findByExternalReference("uuid-ext-ref"))
                                .thenReturn(Optional.of(pendingEvent));
                when(paymentEventRepository.approveIfPending(1L, 99L)).thenReturn(true);

                handleWebhookUseCase.execute(99L);

                verify(coinService).credit(10L, 500);
                verifyNoInteractions(planService);
        }

        @Test
        void execute_shouldActivatePlan_andNotCreditCoins_whenApprovedEventIsPlan() {
                PaymentEvent planEvent = PaymentEvent.builder()
                                .id(1L)
                                .userId(10L)
                                .productId(7L)
                                .externalReference("uuid-ext-ref")
                                .status(PaymentStatus.PENDING)
                                .coinsAmount(0)
                                .productType(ProductType.PLAN)
                                .createdAt(Instant.now())
                                .updatedAt(Instant.now())
                                .build();
                when(paymentEventRepository.findByMpPaymentId(99L)).thenReturn(Optional.empty());
                when(mercadoPagoPort.getPayment(99L))
                                .thenReturn(new MercadoPagoPaymentResult(99L, "uuid-ext-ref", "approved",
                                                "accredited"));
                when(paymentEventRepository.findByExternalReference("uuid-ext-ref"))
                                .thenReturn(Optional.of(planEvent));
                when(paymentEventRepository.approveIfPending(1L, 99L)).thenReturn(true);

                handleWebhookUseCase.execute(99L);

                verify(planService).activate(10L, 7L);
                verifyNoInteractions(coinService);
        }

        @Test
        void execute_shouldActivatePlan_andCreditCoins_whenApprovedPlanHasCoins() {
                PaymentEvent planEvent = PaymentEvent.builder()
                                .id(1L)
                                .userId(10L)
                                .productId(7L)
                                .externalReference("uuid-ext-ref")
                                .status(PaymentStatus.PENDING)
                                .coinsAmount(300)
                                .productType(ProductType.PLAN)
                                .createdAt(Instant.now())
                                .updatedAt(Instant.now())
                                .build();
                when(paymentEventRepository.findByMpPaymentId(99L)).thenReturn(Optional.empty());
                when(mercadoPagoPort.getPayment(99L))
                                .thenReturn(new MercadoPagoPaymentResult(99L, "uuid-ext-ref", "approved",
                                                "accredited"));
                when(paymentEventRepository.findByExternalReference("uuid-ext-ref"))
                                .thenReturn(Optional.of(planEvent));
                when(paymentEventRepository.approveIfPending(1L, 99L)).thenReturn(true);

                handleWebhookUseCase.execute(99L);

                verify(planService).activate(10L, 7L);
                verify(coinService).credit(10L, 300);
        }

        @Test
        void execute_shouldNotActivatePlan_whenApproveIfPendingReturnsFalse_forPlan() {
                PaymentEvent planEvent = PaymentEvent.builder()
                                .id(1L).userId(10L).productId(7L).externalReference("uuid-ext-ref")
                                .status(PaymentStatus.PENDING).coinsAmount(0).productType(ProductType.PLAN)
                                .createdAt(Instant.now()).updatedAt(Instant.now()).build();
                when(paymentEventRepository.findByMpPaymentId(99L)).thenReturn(Optional.empty());
                when(mercadoPagoPort.getPayment(99L))
                                .thenReturn(new MercadoPagoPaymentResult(99L, "uuid-ext-ref", "approved",
                                                "accredited"));
                when(paymentEventRepository.findByExternalReference("uuid-ext-ref"))
                                .thenReturn(Optional.of(planEvent));
                when(paymentEventRepository.approveIfPending(1L, 99L)).thenReturn(false);

                handleWebhookUseCase.execute(99L);

                verifyNoInteractions(planService);
                verifyNoInteractions(coinService);
        }

        @Test
        void execute_shouldCreditCoins_whenPaymentApproved_andEventFoundByPaymentIdAsFallback() {
                when(paymentEventRepository.findByMpPaymentId(99L)).thenReturn(Optional.of(pendingEvent));
                when(mercadoPagoPort.getPayment(99L))
                                .thenReturn(new MercadoPagoPaymentResult(99L, "uuid-ext-ref", "approved",
                                                "accredited"));
                when(paymentEventRepository.findByExternalReference("uuid-ext-ref"))
                                .thenReturn(Optional.empty());
                when(paymentEventRepository.approveIfPending(1L, 99L)).thenReturn(true);

                handleWebhookUseCase.execute(99L);

                verify(coinService).credit(10L, 500);
        }

        @Test
        void execute_shouldNotCreditCoins_whenApproveIfPendingReturnsFalse() {
                when(paymentEventRepository.findByMpPaymentId(99L)).thenReturn(Optional.empty());
                when(mercadoPagoPort.getPayment(99L))
                                .thenReturn(new MercadoPagoPaymentResult(99L, "uuid-ext-ref", "approved",
                                                "accredited"));
                when(paymentEventRepository.findByExternalReference("uuid-ext-ref"))
                                .thenReturn(Optional.of(pendingEvent));
                when(paymentEventRepository.approveIfPending(1L, 99L)).thenReturn(false);

                handleWebhookUseCase.execute(99L);

                verifyNoInteractions(coinService);
        }

        @Test
        void execute_shouldMarkEventFailed_andNotCreditCoins_whenPaymentRejected() {
                when(paymentEventRepository.findByMpPaymentId(99L)).thenReturn(Optional.empty());
                when(mercadoPagoPort.getPayment(99L))
                                .thenReturn(new MercadoPagoPaymentResult(99L, "uuid-ext-ref", "rejected",
                                                "cc_rejected_insufficient_amount"));
                when(paymentEventRepository.findByExternalReference("uuid-ext-ref"))
                                .thenReturn(Optional.of(pendingEvent));

                handleWebhookUseCase.execute(99L);

                verify(paymentEventRepository).updateStatus(eq(1L), eq(PaymentStatus.FAILED), eq(99L), any());
                verifyNoInteractions(coinService);
        }

        @Test
        void execute_shouldMarkEventFailed_whenPaymentCancelled() {
                when(paymentEventRepository.findByMpPaymentId(99L)).thenReturn(Optional.empty());
                when(mercadoPagoPort.getPayment(99L))
                                .thenReturn(new MercadoPagoPaymentResult(99L, "uuid-ext-ref", "cancelled", "expired"));
                when(paymentEventRepository.findByExternalReference("uuid-ext-ref"))
                                .thenReturn(Optional.of(pendingEvent));

                handleWebhookUseCase.execute(99L);

                verify(paymentEventRepository).updateStatus(eq(1L), eq(PaymentStatus.FAILED), eq(99L), any());
                verifyNoInteractions(coinService);
        }

        @Test
        void execute_shouldDoNothing_whenPaymentHasIntermediateStatus() {
                when(paymentEventRepository.findByMpPaymentId(99L)).thenReturn(Optional.empty());
                when(mercadoPagoPort.getPayment(99L))
                                .thenReturn(new MercadoPagoPaymentResult(99L, "uuid-ext-ref", "in_process", null));
                when(paymentEventRepository.findByExternalReference("uuid-ext-ref"))
                                .thenReturn(Optional.of(pendingEvent));

                handleWebhookUseCase.execute(99L);

                verify(paymentEventRepository, never()).approveIfPending(any(), any());
                verify(paymentEventRepository, never()).updateStatus(any(), any(), any(), any());
                verifyNoInteractions(coinService);
        }

        @Test
        void execute_shouldDoNothing_whenPaymentStatusIsPending() {
                when(paymentEventRepository.findByMpPaymentId(99L)).thenReturn(Optional.empty());
                when(mercadoPagoPort.getPayment(99L))
                                .thenReturn(new MercadoPagoPaymentResult(99L, "uuid-ext-ref", "pending", null));
                when(paymentEventRepository.findByExternalReference("uuid-ext-ref"))
                                .thenReturn(Optional.of(pendingEvent));

                handleWebhookUseCase.execute(99L);

                verify(paymentEventRepository, never()).approveIfPending(any(), any());
                verify(paymentEventRepository, never()).updateStatus(any(), any(), any(), any());
                verifyNoInteractions(coinService);
        }

        @Test
        void execute_shouldDoNothing_whenNoEventFound() {
                when(paymentEventRepository.findByMpPaymentId(99L)).thenReturn(Optional.empty());
                when(mercadoPagoPort.getPayment(99L))
                                .thenReturn(new MercadoPagoPaymentResult(99L, "unknown-ref", "approved", "accredited"));
                when(paymentEventRepository.findByExternalReference("unknown-ref"))
                                .thenReturn(Optional.empty());

                handleWebhookUseCase.execute(99L);

                verify(paymentEventRepository, never()).approveIfPending(any(), any());
                verify(paymentEventRepository, never()).updateStatus(any(), any(), any(), any());
                verifyNoInteractions(coinService);
        }

        @Test
        void execute_shouldGrantStoreItemOwnership_andNotCreditCoins_whenApprovedEventIsStoreItem() {
                StoreItem item = StoreItem.builder()
                                .id(3L).name("Cuaderno rosa").description("Un cuaderno rosa")
                                .category(ItemCategory.NOTEBOOK).assetKey("notebook-pink")
                                .priceCoins(50).price(new BigDecimal("1000.00")).build();
                when(paymentEventRepository.findByMpPaymentId(99L)).thenReturn(Optional.empty());
                when(mercadoPagoPort.getPayment(99L))
                                .thenReturn(new MercadoPagoPaymentResult(99L, "uuid-ext-ref", "approved",
                                                "accredited"));
                when(paymentEventRepository.findByExternalReference("uuid-ext-ref"))
                                .thenReturn(Optional.of(storeItemEvent()));
                when(paymentEventRepository.approveIfPending(1L, 99L)).thenReturn(true);
                when(userStoreItemRepository.isOwned(10L, 3L)).thenReturn(false);
                when(storeItemRepository.findById(3L)).thenReturn(Optional.of(item));

                handleWebhookUseCase.execute(99L);

                verify(userStoreItemRepository).save(any(UserStoreItem.class));
                verifyNoInteractions(coinService);
                verifyNoInteractions(planService);
        }

        @Test
        void execute_shouldNotGrantStoreItemTwice_whenAlreadyOwned() {
                when(paymentEventRepository.findByMpPaymentId(99L)).thenReturn(Optional.empty());
                when(mercadoPagoPort.getPayment(99L))
                                .thenReturn(new MercadoPagoPaymentResult(99L, "uuid-ext-ref", "approved",
                                                "accredited"));
                when(paymentEventRepository.findByExternalReference("uuid-ext-ref"))
                                .thenReturn(Optional.of(storeItemEvent()));
                when(paymentEventRepository.approveIfPending(1L, 99L)).thenReturn(true);
                when(userStoreItemRepository.isOwned(10L, 3L)).thenReturn(true);

                handleWebhookUseCase.execute(99L);

                verify(userStoreItemRepository, never()).save(any());
                verify(storeItemRepository, never()).findById(any());
                verifyNoInteractions(coinService);
        }

        @Test
        void execute_shouldNotGrantStoreItem_whenApproveIfPendingReturnsFalse() {
                when(paymentEventRepository.findByMpPaymentId(99L)).thenReturn(Optional.empty());
                when(mercadoPagoPort.getPayment(99L))
                                .thenReturn(new MercadoPagoPaymentResult(99L, "uuid-ext-ref", "approved",
                                                "accredited"));
                when(paymentEventRepository.findByExternalReference("uuid-ext-ref"))
                                .thenReturn(Optional.of(storeItemEvent()));
                when(paymentEventRepository.approveIfPending(1L, 99L)).thenReturn(false);

                handleWebhookUseCase.execute(99L);

                verify(userStoreItemRepository, never()).save(any());
                verifyNoInteractions(coinService);
        }
}
