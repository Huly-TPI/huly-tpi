package com.huly.backend.domain.useCase.payment;

import com.huly.backend.domain.model.payment.PaymentEvent;
import com.huly.backend.domain.model.payment.PaymentPreferenceResult;
import com.huly.backend.domain.model.payment.Product;
import com.huly.backend.domain.exception.BusinessRuleException;
import com.huly.backend.domain.exception.ResourceNotFoundException;
import com.huly.backend.domain.model.enums.PaymentStatus;
import com.huly.backend.domain.model.enums.ProductType;
import com.huly.backend.domain.port.MercadoPagoPort;
import com.huly.backend.domain.repository.payment.PaymentEventRepository;
import com.huly.backend.domain.repository.payment.ProductRepository;
import com.huly.backend.domain.repository.user.UserPlanRepository;
import lombok.RequiredArgsConstructor;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

@RequiredArgsConstructor
public class CreatePaymentPreferenceUseCase {

    private final ProductRepository productRepository;
    private final MercadoPagoPort mercadoPagoPort;
    private final PaymentEventRepository paymentEventRepository;
    private final UserPlanRepository userPlanRepository;

    public PaymentPreferenceResult execute(Long productId, Long userId) {
        Product product = loadProduct(productId);
        validatePlanPurchase(product, userId);

        String externalReference = UUID.randomUUID().toString();
        PaymentPreferenceResult preference = mercadoPagoPort.createPreference(product, userId, externalReference);

        paymentEventRepository.save(buildPendingEvent(userId, product, externalReference, preference.getId()));

        return new PaymentPreferenceResult(preference.getId(), preference.getInitPoint());
    }

    private Product loadProduct(Long productId) {
        return productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("producto", "id", productId));
    }

    /**
     * Impide comprar un plan distinto al que ya está vigente. Permite renovar el mismo
     * plan (mismo producto) y no aplica a productos que no son planes.
     */
    private void validatePlanPurchase(Product product, Long userId) {
        if (product.getType() != ProductType.PLAN) {
            return;
        }
        Instant now = Instant.now();
        userPlanRepository.findByUser(userId)
                .filter(p -> p.isActive(now))
                .filter(active -> !Objects.equals(active.getProductId(), product.getId()))
                .ifPresent(active -> {
                    throw new BusinessRuleException(
                            "Ya tenés un plan activo. No podés comprar otro plan distinto hasta que venza.");
                });
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
