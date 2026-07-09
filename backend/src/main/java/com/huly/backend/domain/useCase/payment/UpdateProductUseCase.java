package com.huly.backend.domain.useCase.payment;

import com.huly.backend.domain.dto.payment.UpdateProductRequest;
import com.huly.backend.domain.model.payment.Product;
import com.huly.backend.domain.repository.payment.ProductRepository;
import com.huly.backend.infrastructure.presentation.exception.NotFoundException;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class UpdateProductUseCase {

    private final ProductRepository productRepository;

    public Product execute(UpdateProductRequest r) {
        Product existing = productRepository.findById(r.id())
                .orElseThrow(() -> new NotFoundException("Producto no encontrado " + r.id()));
        return productRepository.save(Product.builder()
                .id(existing.getId())
                .name(r.name()).description(r.description()).price(r.price())
                .coinsAmount(r.coinsAmount()).type(r.type()).planCode(r.planCode())
                .chatDailyLimit(r.chatDailyLimit()).audioDailyLimit(r.audioDailyLimit())
                .active(existing.isActive())
                .build());
    }
}