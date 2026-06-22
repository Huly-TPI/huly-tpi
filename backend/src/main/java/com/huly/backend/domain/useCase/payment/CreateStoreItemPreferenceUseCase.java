package com.huly.backend.domain.useCase.payment;

import com.huly.backend.domain.exception.BusinessRuleException;
import com.huly.backend.domain.exception.ResourceNotFoundException;
import com.huly.backend.domain.model.enums.PaymentStatus;
import com.huly.backend.domain.model.enums.ProductType;
import com.huly.backend.domain.model.payment.PaymentEvent;
import com.huly.backend.domain.model.payment.PaymentPreferenceResult;
import com.huly.backend.domain.model.payment.Product;
import com.huly.backend.domain.model.shop.StoreItem;
import com.huly.backend.domain.port.MercadoPagoPort;
import com.huly.backend.domain.repository.StoreItemRepository;
import com.huly.backend.domain.repository.UserStoreItemRepository;
import com.huly.backend.domain.repository.payment.PaymentEventRepository;
import lombok.RequiredArgsConstructor;

import java.time.Instant;
import java.util.UUID;

@RequiredArgsConstructor
public class CreateStoreItemPreferenceUseCase {

    private final StoreItemRepository storeItemRepository;
    private final MercadoPagoPort mercadoPagoPort;
    private final PaymentEventRepository paymentEventRepository;
    private final UserStoreItemRepository userStoreItemRepository;

    public PaymentPreferenceResult execute(Long storeItemId, Long userId) {
        StoreItem item = storeItemRepository.findById(storeItemId)
                .orElseThrow(() -> new ResourceNotFoundException("item", "id", storeItemId));

        if (item.getPrice() == null) {
            throw new BusinessRuleException("Este item no se puede comprar con dinero real");
        }
        if (userStoreItemRepository.isOwned(userId, storeItemId)) {
            throw new BusinessRuleException("Ya tenés este item");
        }

        String externalReference = UUID.randomUUID().toString();
        PaymentPreferenceResult preference = mercadoPagoPort.createPreference(toProduct(item), userId,
                externalReference);

        paymentEventRepository.save(buildPendingEvent(userId, item, externalReference, preference.getId()));

        return new PaymentPreferenceResult(preference.getId(), preference.getInitPoint());
    }

    private Product toProduct(StoreItem item) {
        return Product.builder()
                .id(item.getId())
                .name(item.getName())
                .description(item.getDescription())
                .price(item.getPrice())
                .coinsAmount(0)
                .type(ProductType.STORE_ITEM)
                .build();
    }

    private PaymentEvent buildPendingEvent(Long userId, StoreItem item, String externalReference,
            String mpPreferenceId) {
        Instant now = Instant.now();
        return PaymentEvent.builder()
                .userId(userId)
                .storeItemId(item.getId())
                .externalReference(externalReference)
                .mpPreferenceId(mpPreferenceId)
                .status(PaymentStatus.PENDING)
                .coinsAmount(0)
                .productType(ProductType.STORE_ITEM)
                .createdAt(now)
                .updatedAt(now)
                .build();
    }

}
