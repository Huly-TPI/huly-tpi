package com.huly.backend.infrastructure.repository.jpaRepository.implementation;
import com.huly.backend.domain.model.user.UserStoreItem;
import com.huly.backend.domain.repository.UserStoreItemRepository;
import com.huly.backend.infrastructure.repository.entity.StoreItemEntity;
import com.huly.backend.infrastructure.repository.entity.UserStoreItemEntity;
import com.huly.backend.infrastructure.repository.jpaRepository.interfaces.IStoreItemJpaRepository;
import com.huly.backend.infrastructure.repository.jpaRepository.interfaces.IUserStoreItemJpaRepository;
import com.huly.backend.infrastructure.repository.mapper.UserStoreItemMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
@Component
@RequiredArgsConstructor

public class UserStoreItemRepositoryImpl implements UserStoreItemRepository {

    private final IUserStoreItemJpaRepository userStoreItemJpaRepository;
    private final IStoreItemJpaRepository storeItemJpaRepository;
    private final UserStoreItemMapper userStoreItemMapper;

    @Override
    public List<UserStoreItem> findAllByUserId(Long userId) {
        return userStoreItemJpaRepository.findAllByUserId(userId).stream().map(userStoreItemMapper::toDomain).toList();
    }

    @Override
    public boolean isOwned(Long userId, Long storeItemId) {
        return userStoreItemJpaRepository.existsByUserIdAndStoreItemId(userId, storeItemId);
    }

    @Override
    public UserStoreItem save(UserStoreItem userStoreItem) {
        StoreItemEntity itemRef = storeItemJpaRepository.getReferenceById(userStoreItem.getStoreItem().getId());
        UserStoreItemEntity entity = UserStoreItemEntity.builder() 
            .userId(userStoreItem.getUserId())
            .storeItem(itemRef)
            .equipped(userStoreItem.isEquipped())
            .acquiredAt(userStoreItem.getAcquiredAt())
            .build();
        return userStoreItemMapper.toDomain(userStoreItemJpaRepository.save(entity));
    }

    @Override
    @Transactional
    public void updateEquipped(Long userId, Long storeItemId, boolean equipped) {
        userStoreItemJpaRepository.updateEquipped(userId, storeItemId, equipped);
    }

    
}
