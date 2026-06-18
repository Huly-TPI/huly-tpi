package com.huly.backend.infrastructure.presentation.controller;

import com.huly.backend.domain.useCase.payment.HandleWebhookUseCase;
import com.huly.backend.infrastructure.adapter.mercadopago.MercadoPagoSignatureValidator;
import com.huly.backend.infrastructure.presentation.dto.payment.WebhookNotificationDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/webhook")
@RequiredArgsConstructor
public class MercadoPagoWebhookController {

    private final HandleWebhookUseCase handleWebhookUseCase;
    private final MercadoPagoSignatureValidator signatureValidator;

    @PostMapping("/mercadopago")
    public ResponseEntity<Void> handleWebhook(
            @RequestHeader(value = "x-signature", required = false) String xSignature,
            @RequestHeader(value = "x-request-id", required = false) String xRequestId,
            @RequestParam(value = "data.id", required = false) String dataIdParam,
            @RequestBody WebhookNotificationDto notification) {

        log.info("MP Webhook raw — x-signature={} x-request-id={} data.id(param)={} body.data.id={}",
                xSignature, xRequestId, dataIdParam, extractDataId(notification));

        if (!validateSignature(xSignature, xRequestId, dataIdParam, notification)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        logWebhookReceived(notification);

        if (!isPaymentNotification(notification)) {
            return ResponseEntity.ok().build();
        }

        return processPaymentNotification(notification.data().id());
    }

    // --- Private methods ---

    private ResponseEntity<Void> processPaymentNotification(String rawId) {
        Long mpPaymentId = parsePaymentId(rawId);
        if (mpPaymentId == null) {
            return ResponseEntity.badRequest().build();
        }

        try {
            handleWebhookUseCase.execute(mpPaymentId);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            log.error("Error processing webhook for paymentId={}", mpPaymentId, e);
            return ResponseEntity.internalServerError().build();
        }
    }

    private boolean validateSignature(String xSignature, String xRequestId, String dataIdParam, WebhookNotificationDto notification) {
        String dataId = dataIdParam != null ? dataIdParam : extractDataId(notification);
        boolean valid = signatureValidator.isValid(xSignature, xRequestId, dataId);
        if (!valid) {
            log.warn("Invalid or missing MP webhook signature, requestId={}", xRequestId);
        }
        return valid;
    }

    private void logWebhookReceived(WebhookNotificationDto notification) {
        log.info("MP Webhook received: type={} action={} paymentId={}",
                notification.type(),
                notification.action(),
                extractDataId(notification));
    }

    private boolean isPaymentNotification(WebhookNotificationDto notification) {
        return "payment".equals(notification.type())
                && notification.data() != null
                && notification.data().id() != null;
    }


    private Long parsePaymentId(String rawId) {
        try {
            return Long.parseLong(rawId);
        } catch (NumberFormatException e) {
            log.warn("Invalid payment id in webhook: {}", rawId);
            return null;
        }
    }

    private String extractDataId(WebhookNotificationDto notification) {
        return notification.data() != null ? notification.data().id() : null;
    }
}
