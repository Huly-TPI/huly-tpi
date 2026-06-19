package com.huly.backend.infrastructure.repository.mapper;
import com.huly.backend.domain.model.UserStoreItem;
import com.huly.backend.domain.model.enums.ItemCategory;
import com.huly.backend.infrastructure.repository.entity.StoreItemEntity;
import com.huly.backend.infrastructure.repository.entity.UserStoreItemEntity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import static org.assertj.core.api.Assertions.assertThat;
class UserStoreItemMapperTest {

    private UserStoreItemMapper userStoreItemMapper;

    @BeforeEach
    void setUp() {
        userStoreItemMapper = new UserStoreItemMapper();
    }

    @Test 
    void toDomain_shouldMapAllFields() {
        Instant now = Instant.now();
        StoreItemEntity itemEntity = StoreItemEntity.builder() 
                    .id(10L).name("Casa rosa").description("Casa de color rosa")
                    .category(ItemCategory.HOUSE).assetKey("casa-rosa").priceCoins(50)
                    .build();
        UserStoreItemEntity entity = UserStoreItemEntity.builder()
                    .id(1L).userId(7L).storeItem(itemEntity).equipped(true).acquiredAt(now)
                    .build();  
                
        UserStoreItem result = userStoreItemMapper.toDomain(entity);

        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getUserId()).isEqualTo(7L);
        assertThat(result.isEquipped()).isTrue();
        assertThat(result.getAcquiredAt()).isEqualTo(now);
        assertThat(result.getStoreItem().getId()).isEqualTo(10L);
        assertThat(result.getStoreItem().getAssetKey()).isEqualTo("casa-rosa");
        assertThat(result.getStoreItem().getCategory()).isEqualTo(ItemCategory.HOUSE);
    }

    @Test 
    void toDomain_shouldReturnNull_whenEntityIsNull() {
        assertThat(userStoreItemMapper.toDomain(null)).isNull();
    }

    @Test 
    void toDomain_shouldMapNullStoreItem_whenStoreItemIsNull() {
        UserStoreItemEntity entity = UserStoreItemEntity.builder()
                    .id(1L).userId(7L).equipped(false).acquiredAt(Instant.now())
                    .build();  
        UserStoreItem result = userStoreItemMapper.toDomain(entity);
        assertThat(result.getStoreItem()).isNull();
    }
    
}
