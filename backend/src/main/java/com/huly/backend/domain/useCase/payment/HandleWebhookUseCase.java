package com.huly.backend.domain.useCase.payment;

import com.huly.backend.domain.dto.payment.MercadoPagoPaymentResult;
import com.huly.backend.domain.dto.payment.PaymentEvent;
import com.huly.backend.domain.model.enums.PaymentStatus;
import com.huly.backend.domain.port.MercadoPagoPort;
import com.huly.backend.domain.repository.PaymentEventRepository;
import com.huly.backend.domain.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class HandleWebhookUseCase {

    private final PaymentEventRepository paymentEventRepository;
    private final MercadoPagoPort mercadoPagoPort;
    private final UserRepository userRepository;

    @Transactional
    public void execute(Long mpPaymentId) {
        // Idempotency: si ya está aprobado, ignorar
        Optional<PaymentEvent> byPaymentId = paymentEventRepository.findByMpPaymentId(mpPaymentId);
        if (byPaymentId.isPresent() && byPaymentId.get().getStatus() == PaymentStatus.APPROVED) {
            log.info("Payment {} already APPROVED, skipping", mpPaymentId);
            return;
        }

        MercadoPagoPaymentResult payment = mercadoPagoPort.getPayment(mpPaymentId);

        // externalReference = UUID que guardamos como mpPreferenceId en PaymentEvent
        PaymentEvent event = paymentEventRepository.findByMpPreferenceId(payment.getExternalReference())
                .orElseGet(() -> byPaymentId.orElse(null));

        if (event == null) {
            log.warn("No payment_event found for externalReference={} paymentId={}", payment.getExternalReference(), mpPaymentId);
            return;
        }

        if ("approved".equals(payment.getStatus())) {
            paymentEventRepository.updateStatus(event.getId(), PaymentStatus.APPROVED, mpPaymentId, null);
            userRepository.addCoins(event.getUserId(), event.getCoinsAmount());
            log.info("Payment {} APPROVED — credited {} coins to user {}", mpPaymentId, event.getCoinsAmount(), event.getUserId());
        } else {
            String detail = payment.getStatus() + ": " + payment.getStatusDetail();
            paymentEventRepository.updateStatus(event.getId(), PaymentStatus.FAILED, mpPaymentId, detail);
            log.info("Payment {} FAILED — status={} detail={}", mpPaymentId, payment.getStatus(), payment.getStatusDetail());
        }
    }
}
