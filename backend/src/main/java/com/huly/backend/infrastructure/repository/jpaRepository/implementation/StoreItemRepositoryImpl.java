package com.huly.backend.infrastructure.repository.jpaRepository.implementation;
import com.huly.backend.domain.model.shop.StoreItem;
import com.huly.backend.domain.repository.StoreItemRepository;
import com.huly.backend.infrastructure.repository.entity.StoreItemEntity;
import com.huly.backend.infrastructure.repository.jpaRepository.interfaces.IStoreItemJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class StoreItemRepositoryImpl implements StoreItemRepository {

    private final IStoreItemJpaRepository jpaRepository;
    
    @Override
    public List<StoreItem> findAll() {
         return jpaRepository.findAll().stream().map(this::toDomain).toList();
    }

    @Override
    public Optional<StoreItem> findById(Long id) {
        return jpaRepository.findById(id).map(this::toDomain);
    }

    private StoreItem toDomain(StoreItemEntity entity) {
        return StoreItem.builder()
                .id(entity.getId())
                .name(entity.getName())
                .description(entity.getDescription())
                .category(entity.getCategory())
                .assetKey(entity.getAssetKey())
                .priceCoins(entity.getPriceCoins())
                .price(entity.getPrice())
                .build();
    }
    
}
