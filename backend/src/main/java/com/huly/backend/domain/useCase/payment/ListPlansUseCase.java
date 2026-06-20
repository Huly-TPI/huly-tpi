package com.huly.backend.domain.useCase.payment;

import com.huly.backend.domain.model.payment.Product;
import com.huly.backend.domain.model.enums.ProductType;
import com.huly.backend.domain.repository.payment.ProductRepository;
import lombok.RequiredArgsConstructor;

import java.util.List;

@RequiredArgsConstructor
public class ListPlansUseCase {

    private final ProductRepository productRepository;

    public List<Product> execute() {
        return productRepository.findByType(ProductType.PLAN);
    }
}
