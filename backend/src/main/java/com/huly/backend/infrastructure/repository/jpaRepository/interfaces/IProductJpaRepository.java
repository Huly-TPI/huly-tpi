package com.huly.backend.infrastructure.repository.jpaRepository.interfaces;

import com.huly.backend.domain.model.enums.ProductType;
import com.huly.backend.infrastructure.repository.entity.ProductEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface IProductJpaRepository extends JpaRepository<ProductEntity, Long> {
    List<ProductEntity> findByType(ProductType type);
    List<ProductEntity> findByTypeAndActive(ProductType type, boolean active);
}
