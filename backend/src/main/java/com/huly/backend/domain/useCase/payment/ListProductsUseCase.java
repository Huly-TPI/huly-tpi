package com.huly.backend.domain.useCase.payment;

import com.huly.backend.domain.dto.payment.Product;
import com.huly.backend.domain.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import java.util.List;

@RequiredArgsConstructor
public class ListProductsUseCase {

    private final ProductRepository productRepository;

    public List<Product> execute() {
        return productRepository.findAll();
    }
}
