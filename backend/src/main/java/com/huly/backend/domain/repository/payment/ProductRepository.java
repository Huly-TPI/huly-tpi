package com.huly.backend.domain.repository.payment;

import com.huly.backend.domain.model.payment.Product;
import com.huly.backend.domain.model.enums.ProductType;

import java.util.List;
import java.util.Optional;

public interface ProductRepository {
    List<Product> findAll();
    Optional<Product> findById(Long id);
    List<Product> findByIds(List<Long> ids);
    List<Product> findByType(ProductType type);
}
