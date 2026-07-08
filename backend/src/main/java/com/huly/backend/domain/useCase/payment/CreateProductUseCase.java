package com.huly.backend.domain.useCase.payment;

import com.huly.backend.domain.dto.payment.CreateProductRequest;
import com.huly.backend.domain.model.payment.Product;
import com.huly.backend.domain.repository.payment.ProductRepository;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class CreateProductUseCase {

    private final ProductRepository productRepository;

    public Product execute(CreateProductRequest r) {
        return productRepository.save(Product.builder()
                .name(r.name()).description(r.description()).price(r.price())
                .coinsAmount(r.coinsAmount()).type(r.type()).planCode(r.planCode())
                .chatDailyLimit(r.chatDailyLimit()).audioDailyLimit(r.audioDailyLimit())
                .active(true)
                .build());
    }
}