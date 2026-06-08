package com.huly.backend.infrastructure.presentation.controller;

import com.huly.backend.domain.useCase.payment.HandleWebhookUseCase;
import com.huly.backend.infrastructure.presentation.dto.payment.WebhookNotificationDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/webhook")
@RequiredArgsConstructor
public class MercadoPagoWebhookController {

    private final HandleWebhookUseCase handleWebhookUseCase;

    @PostMapping("/mercadopago")
    public ResponseEntity<Void> handleWebhook(@RequestBody WebhookNotificationDto notification) {
        log.info("MP Webhook received: type={} action={} paymentId={}",
                notification.type(),
                notification.action(),
                notification.data() != null ? notification.data().id() : "null");

        if (!"payment".equals(notification.type()) || notification.data() == null || notification.data().id() == null) {
            return ResponseEntity.ok().build();
        }

        try {
            Long mpPaymentId = Long.parseLong(notification.data().id());
            handleWebhookUseCase.execute(mpPaymentId);
        } catch (NumberFormatException e) {
            log.warn("Invalid payment id in webhook: {}", notification.data().id());
        } catch (Exception e) {
            log.error("Error processing webhook for paymentId={}", notification.data().id(), e);
        }

        return ResponseEntity.ok().build();
    }
}
