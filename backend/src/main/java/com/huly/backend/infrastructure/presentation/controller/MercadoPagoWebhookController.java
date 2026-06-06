package com.huly.backend.infrastructure.presentation.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/webhook")
public class MercadoPagoWebhookController {

    @PostMapping("/mercadopago")
    public ResponseEntity<Void> handleWebhook(
            @RequestBody String rawBody,
            @RequestHeader HttpHeaders headers) {

        log.info("MP Webhook raw body: {}", rawBody);
        log.info("MP Webhook headers: {}", headers);

        return ResponseEntity.ok().build();
    }
}
