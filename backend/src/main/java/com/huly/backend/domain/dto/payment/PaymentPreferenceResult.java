package com.huly.backend.domain.dto.payment;


import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class PaymentPreferenceResult {
    private final String id;
    private final String initPoint;
}
