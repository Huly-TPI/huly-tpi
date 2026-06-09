package com.huly.backend.domain.useCase.payment;

import com.huly.backend.domain.dto.payment.MercadoPagoPaymentResult;
import com.huly.backend.domain.dto.payment.PaymentEvent;
import com.huly.backend.domain.model.enums.PaymentStatus;
import com.huly.backend.domain.port.MercadoPagoPort;
import com.huly.backend.domain.repository.PaymentEventRepository;
import com.huly.backend.domain.service.payment.CoinService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.Optional;
import java.util.Set;

@Slf4j
@Service
@RequiredArgsConstructor
public class HandleWebhookUseCase {

    private static final Set<String> TERMINAL_FAILURE_STATUSES =
            Set.of("rejected", "cancelled", "refunded", "charged_back");

    private final PaymentEventRepository paymentEventRepository;
    private final MercadoPagoPort mercadoPagoPort;
    private final CoinService coinService;

    @Transactional
    public void execute(Long mpPaymentId) {
        Optional<PaymentEvent> byPaymentId = paymentEventRepository.findByMpPaymentId(mpPaymentId);
        if (byPaymentId.isPresent() && byPaymentId.get().getStatus() == PaymentStatus.APPROVED) {
            log.info("Payment {} already APPROVED, skipping", mpPaymentId);
            return;
        }

        MercadoPagoPaymentResult payment = mercadoPagoPort.getPayment(mpPaymentId);
        PaymentEvent event = resolveEvent(byPaymentId, payment.getExternalReference(), mpPaymentId);
        if (event == null) return;

        if ("approved".equals(payment.getStatus())) {
            processApproved(event, mpPaymentId);
        } else if (TERMINAL_FAILURE_STATUSES.contains(payment.getStatus())) {
            processFailed(event, mpPaymentId, payment);
        } else {
            log.info("Payment {} has intermediate status={}, no action taken", mpPaymentId, payment.getStatus());
        }
    }

    private PaymentEvent resolveEvent(Optional<PaymentEvent> byPaymentId, String externalReference, Long mpPaymentId) {
        PaymentEvent event = paymentEventRepository.findByExternalReference(externalReference)
                .orElseGet(() -> byPaymentId.orElse(null));
        if (event == null) {
            log.warn("No payment_event found for externalReference={} paymentId={}", externalReference, mpPaymentId);
        }
        return event;
    }

    private void processApproved(PaymentEvent event, Long mpPaymentId) {
        boolean credited = paymentEventRepository.approveIfPending(event.getId(), mpPaymentId);
        if (credited) {
            coinService.credit(event.getUserId(), event.getCoinsAmount());
            log.info("Payment {} APPROVED — credited {} coins to user {}", mpPaymentId, event.getCoinsAmount(), event.getUserId());
        } else {
            log.info("Payment {} already APPROVED by concurrent webhook, skipping coin credit", mpPaymentId);
        }
    }

    private void processFailed(PaymentEvent event, Long mpPaymentId, MercadoPagoPaymentResult payment) {
        String detail = payment.getStatus() + ": " + payment.getStatusDetail();
        paymentEventRepository.updateStatus(event.getId(), PaymentStatus.FAILED, mpPaymentId, detail);
        log.info("Payment {} FAILED — status={} detail={}", mpPaymentId, payment.getStatus(), payment.getStatusDetail());
    }
}
