package com.huly.backend.infrastructure.presentation.dto.admin;

import lombok.Builder;
import lombok.Getter;
import java.math.BigDecimal;
import java.util.List;

@Getter
@Builder
public class UserFinancialsResponse {
    private List<PaymentEventDto> paymentEvents;
    private BigDecimal totalEarnings;
}
