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
         return jpaRepository.findAllByOrderByIdAsc().stream().map(this::toDomain).toList();
    }

    @Override
    public Optional<StoreItem> findById(Long id) {
        return jpaRepository.findById(id).map(this::toDomain);
    }

    @Override 
    public StoreItem save(StoreItem item) { 
        StoreItemEntity saved = jpaRepository.save(toEntity(item));
        return toDomain(saved);
    }

    @Override
    public void deleteById(Long id) {
        jpaRepository.deleteById(id);
    }

    private StoreItemEntity toEntity(StoreItem item) {
        return StoreItemEntity.builder()
                .id(item.getId())
                .name(item.getName())
                .description(item.getDescription())
                .category(item.getCategory())
                .assetKey(item.getAssetKey())
                .priceCoins(item.getPriceCoins())
                .price(item.getPrice())
                .premiumOnly(item.isPremiumOnly())
                .imageUrl(item.getImageUrl())
                .build();
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
                .premiumOnly(entity.isPremiumOnly())
                .imageUrl(entity.getImageUrl())
                .build();
    }
    
}
