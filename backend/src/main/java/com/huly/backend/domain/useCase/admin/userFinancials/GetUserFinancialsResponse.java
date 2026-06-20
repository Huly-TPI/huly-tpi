package com.huly.backend.domain.useCase.admin.userFinancials;

import java.math.BigDecimal;
import java.util.List;

public record GetUserFinancialsResponse(
        List<PaymentEventResponse> paymentEvents,
        BigDecimal totalEarnings
) {
}
