package com.huly.backend.infrastructure.repository.jpaRepository.implementation;

import com.huly.backend.domain.model.payment.Product;
import com.huly.backend.domain.model.enums.ProductType;
import com.huly.backend.domain.repository.payment.ProductRepository;
import com.huly.backend.infrastructure.repository.entity.ProductEntity;
import com.huly.backend.infrastructure.repository.jpaRepository.interfaces.IProductJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class ProductRepositoryImpl implements ProductRepository {

    private final IProductJpaRepository jpaRepository;

    @Override
    public List<Product> findAll() {
        return jpaRepository.findAll()
                .stream()
                .map(this::toDomain)
                .toList();
    }

    @Override
    public Optional<Product> findById(Long id) {
        return jpaRepository.findById(id).map(this::toDomain);
    }

    @Override
    public List<Product> findByIds(List<Long> ids) {
        return jpaRepository.findAllById(ids)
                .stream()
                .map(this::toDomain)
                .toList();
    }

    @Override
    public List<Product> findByType(ProductType type) {
        return jpaRepository.findByType(type)
                .stream()
                .map(this::toDomain)
                .toList();
    }

    @Override
    public Product save(Product product) {
        return toDomain(jpaRepository.save(toEntity(product)));
    }

    @Override
    public List<Product> findByTypeAndActive(ProductType type, boolean active) {
        return jpaRepository.findByTypeAndActive(type, active).stream().map(this::toDomain).toList();
    }

    private ProductEntity toEntity(Product p) {
        return ProductEntity.builder()
                .id(p.getId())
                .name(p.getName())
                .description(p.getDescription())
                .price(p.getPrice())
                .coinsAmount(p.getCoinsAmount())
                .type(p.getType())
                .planCode(p.getPlanCode())
                .chatDailyLimit(p.getChatDailyLimit())
                .audioDailyLimit(p.getAudioDailyLimit())
                .active(p.isActive())
                .build();
    }

    private Product toDomain(ProductEntity entity) {
        return Product.builder()
                .id(entity.getId())
                .name(entity.getName())
                .description(entity.getDescription())
                .price(entity.getPrice())
                .coinsAmount(entity.getCoinsAmount())
                .type(entity.getType())
                .planCode(entity.getPlanCode())
                .chatDailyLimit(entity.getChatDailyLimit())
                .audioDailyLimit(entity.getAudioDailyLimit())
                .active(entity.isActive())
                .build();
    }
}
