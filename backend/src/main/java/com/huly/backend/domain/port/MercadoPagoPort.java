package com.huly.backend.domain.port;

import com.huly.backend.domain.dto.payment.MercadoPagoPaymentResult;
import com.huly.backend.domain.dto.payment.PaymentPreferenceResult;
import com.huly.backend.domain.dto.payment.Product;

public interface MercadoPagoPort {
    PaymentPreferenceResult createPreference(Product product, Long userId, String externalReference);
    MercadoPagoPaymentResult getPayment(Long paymentId);
}
