package com.huly.backend.domain.dto.payment;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class MercadoPagoPaymentResult {
    private Long paymentId;
    private String externalReference;
    private String status;
    private String statusDetail;
}
