package com.huly.backend.domain.repository;

import com.huly.backend.domain.dto.payment.Product;

import java.util.List;
import java.util.Optional;

public interface ProductRepository {
    List<Product> findAll();
    Optional<Product> findById(Long id);
}
