package com.huly.backend.domain.useCase.payment;

import com.huly.backend.domain.dto.payment.PaymentPreferenceResult;
import com.huly.backend.domain.exception.ResourceNotFoundException;
import com.huly.backend.domain.dto.payment.Product;
import com.huly.backend.domain.port.MercadoPagoPort;
import com.huly.backend.domain.repository.ProductRepository;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class CreatePaymentPreferenceUseCase {

    private final ProductRepository productRepository;
    private final MercadoPagoPort mercadoPagoPort;


    public PaymentPreferenceResult execute(Long productId, Long userId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("producto", "id", productId));

        PaymentPreferenceResult preference = mercadoPagoPort.createPreference(product,userId);
        return new PaymentPreferenceResult(preference.getId(), preference.getInitPoint());
    }
}