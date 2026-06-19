package com.huly.backend.infrastructure.repository.mapper;
import com.huly.backend.domain.model.shop.StoreItem;
import com.huly.backend.domain.model.user.UserStoreItem;
import com.huly.backend.infrastructure.repository.entity.StoreItemEntity;
import com.huly.backend.infrastructure.repository.entity.UserStoreItemEntity;
import org.springframework.stereotype.Component;

@Component
public class UserStoreItemMapper {

    public UserStoreItem toDomain(UserStoreItemEntity entity) {
        if (entity == null) {
            return null;
        }
        return UserStoreItem.builder()
                .id(entity.getId())
                .userId(entity.getUserId())
                .storeItem(toDomain(entity.getStoreItem()))
                .equipped(entity.isEquipped())
                .acquiredAt(entity.getAcquiredAt())
                .build();
    }

    private StoreItem toDomain(StoreItemEntity entity) {
        if (entity == null) {
            return null;
        }
    
    return StoreItem.builder()
    .id(entity.getId())
    .name(entity.getName())
    .description(entity.getDescription())
    .category(entity.getCategory())
    .assetKey(entity.getAssetKey())
    .priceCoins(entity.getPriceCoins())
    .build();
    }


    
}
