package com.huly.backend.domain.useCase.payment;

import com.huly.backend.domain.model.payment.Product;
import com.huly.backend.domain.repository.payment.ProductRepository;
import com.huly.backend.infrastructure.presentation.exception.NotFoundException;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class SetProductActiveUseCase {

    private final ProductRepository productRepository;

    public Product execute(Long id, boolean active) {
        Product p = productRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Producto no encontrado " + id));
        return productRepository.save(Product.builder()
                .id(p.getId()).name(p.getName()).description(p.getDescription())
                .price(p.getPrice()).coinsAmount(p.getCoinsAmount()).type(p.getType())
                .planCode(p.getPlanCode()).chatDailyLimit(p.getChatDailyLimit())
                .audioDailyLimit(p.getAudioDailyLimit())
                .active(active)
                .build());
    }
}