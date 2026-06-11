package com.huly.backend.domain.useCase.payment;

import com.huly.backend.domain.dto.payment.PaymentEvent;
import com.huly.backend.domain.dto.payment.PaymentPreferenceResult;
import com.huly.backend.domain.dto.payment.Product;
import com.huly.backend.domain.exception.ResourceNotFoundException;
import com.huly.backend.domain.model.enums.PaymentStatus;
import com.huly.backend.domain.port.MercadoPagoPort;
import com.huly.backend.domain.repository.PaymentEventRepository;
import com.huly.backend.domain.repository.ProductRepository;
import lombok.RequiredArgsConstructor;

import java.time.Instant;
import java.util.UUID;

@RequiredArgsConstructor
public class CreatePaymentPreferenceUseCase {

    private final ProductRepository productRepository;
    private final MercadoPagoPort mercadoPagoPort;
    private final PaymentEventRepository paymentEventRepository;

    public PaymentPreferenceResult execute(Long productId, Long userId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("producto", "id", productId));

        String externalReference = UUID.randomUUID().toString();
        PaymentPreferenceResult preference = mercadoPagoPort.createPreference(product, userId, externalReference);

        paymentEventRepository.save(buildPendingEvent(userId, product, externalReference, preference.getId()));

        return new PaymentPreferenceResult(preference.getId(), preference.getInitPoint());
    }

    private PaymentEvent buildPendingEvent(Long userId, Product product, String externalReference,
                                           String mpPreferenceId) {
        Instant now = Instant.now();
        return PaymentEvent.builder()
                .userId(userId)
                .productId(product.getId())
                .externalReference(externalReference)
                .mpPreferenceId(mpPreferenceId)
                .status(PaymentStatus.PENDING)
                .coinsAmount(product.getCoinsAmount())
                .productType(product.getType())
                .createdAt(now)
                .updatedAt(now)
                .build();
    }
}
