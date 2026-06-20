package com.huly.backend.domain.port;

import com.huly.backend.domain.model.payment.MercadoPagoPaymentResult;
import com.huly.backend.domain.model.payment.PaymentPreferenceResult;
import com.huly.backend.domain.model.payment.Product;

public interface MercadoPagoPort {
    PaymentPreferenceResult createPreference(Product product, Long userId, String externalReference);
    MercadoPagoPaymentResult getPayment(Long paymentId);
}
