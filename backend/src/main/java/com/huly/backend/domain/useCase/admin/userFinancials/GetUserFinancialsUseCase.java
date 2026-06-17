package com.huly.backend.domain.useCase.admin.userFinancials;

import com.huly.backend.domain.dto.payment.PaymentEvent;
import com.huly.backend.domain.dto.payment.Product;
import com.huly.backend.domain.model.enums.PaymentStatus;
import com.huly.backend.domain.repository.PaymentEventRepository;
import com.huly.backend.domain.repository.ProductRepository;
import com.huly.backend.domain.repository.UserRepository;
import lombok.RequiredArgsConstructor;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@RequiredArgsConstructor
public class GetUserFinancialsUseCase {

    private final UserRepository userRepository;
    private final PaymentEventRepository paymentEventRepository;
    private final ProductRepository productRepository;

    public GetUserFinancialsResponse execute(GetUserFinancialsRequest request) {
        Long userId = request.userId();

        userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        List<PaymentEvent> payments = paymentEventRepository.findByUserId(userId);
        List<Long> productIds = new ArrayList<>();
        for (PaymentEvent payment : payments) {
            if (payment.getProductId() != null && !productIds.contains(payment.getProductId())) {
                productIds.add(payment.getProductId());
            }
        }

        List<Product> products = productRepository.findByIds(productIds);
        Map<Long, Product> productMap = products.stream()
                .collect(Collectors.toMap(Product::getId, p -> p, (p1, p2) -> p1));

        List<PaymentEventResponse> paymentEvents = payments.stream()
                .map(payment -> {
                    Product product = requireProduct(productMap, payment.getProductId(), payment.getId());
                    return new PaymentEventResponse(
                            payment.getId(),
                            payment.getProductId(),
                            product.getName(),
                            product.getPrice(),
                            payment.getExternalReference(),
                            payment.getMpPaymentId(),
                            Objects.requireNonNull(payment.getStatus(), "PaymentEvent status is required").name(),
                            payment.getCoinsAmount(),
                            Objects.requireNonNull(payment.getProductType(), "PaymentEvent productType is required").name(),
                            payment.getCreatedAt()
                    );
                })
                .toList();

        BigDecimal totalEarnings = BigDecimal.ZERO;
        List<PaymentEvent> approvedPayments = paymentEventRepository.findByUserIdAndStatus(userId, PaymentStatus.APPROVED);
        for (PaymentEvent approvedPayment : approvedPayments) {
            Product product = requireProduct(productMap, approvedPayment.getProductId(), approvedPayment.getId());
            totalEarnings = totalEarnings.add(Objects.requireNonNull(product.getPrice(), "Product price is required"));
        }

        return new GetUserFinancialsResponse(paymentEvents, totalEarnings);
    }

    private Product requireProduct(Map<Long, Product> productMap, Long productId, Long paymentEventId) {
        Product product = productMap.get(productId);
        if (product == null) {
            throw new IllegalStateException("Missing product for paymentEventId=" + paymentEventId + ", productId=" + productId);
        }
        return product;
    }
}
