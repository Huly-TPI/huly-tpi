package com.huly.backend.infrastructure.repository.jpaRepository.implementation;
import com.huly.backend.domain.model.StoreItem;
import com.huly.backend.domain.model.UserStoreItem;
import com.huly.backend.infrastructure.repository.entity.StoreItemEntity;
import com.huly.backend.infrastructure.repository.entity.UserStoreItemEntity;
import com.huly.backend.infrastructure.repository.jpaRepository.interfaces.IStoreItemJpaRepository;
import com.huly.backend.infrastructure.repository.jpaRepository.interfaces.IUserStoreItemJpaRepository;
import com.huly.backend.infrastructure.repository.mapper.UserStoreItemMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class UserStoreItemRepositoryImplTest {

    @Mock 
    private IUserStoreItemJpaRepository userStoreItemJpaRepository;
    @Mock
    private IStoreItemJpaRepository storeItemJpaRepository;
    @Mock
    private UserStoreItemMapper userStoreItemMapper;
    @InjectMocks
    private UserStoreItemRepositoryImpl userStoreItemRepository;

    @Test 
    void findAllByUserId_shouldReturnMappedList() {
        UserStoreItemEntity entity = UserStoreItemEntity.builder().id(1L).userId(7L).build();
        when(userStoreItemJpaRepository.findAllByUserId(7L)).thenReturn(List.of(entity));
        when(userStoreItemMapper.toDomain(entity)).thenReturn(UserStoreItem.builder().id(1L).userId(7L).build());
        List<UserStoreItem> result = userStoreItemRepository.findAllByUserId(7L);
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getUserId()).isEqualTo(7L);
        verify(userStoreItemJpaRepository).findAllByUserId(7L);
    }

    @Test 
    void isOwned_shouldDelegateToJpaRepository() {
        when(userStoreItemJpaRepository.existsByUserIdAndStoreItemId(7L, 10L)).thenReturn(true);
        assertThat(userStoreItemRepository.isOwned(7L, 10L)).isTrue();
        verify(userStoreItemJpaRepository).existsByUserIdAndStoreItemId(7L, 10L);
    }

    @Test 
    void save_shouldBuildEntityWithReferenceAndReturnMapped() {
        Instant now = Instant.now();
        UserStoreItem domain = UserStoreItem.builder()
                .userId(7L)
                .storeItem(StoreItem.builder().id(10L).build())
                .equipped(false)
                .acquiredAt(now)
                .build();
        StoreItemEntity itemRef = StoreItemEntity.builder().id(10L).build();
        UserStoreItemEntity savedEntity = UserStoreItemEntity.builder().id(99L).userId(7L).build();
        when(storeItemJpaRepository.getReferenceById(10L)).thenReturn(itemRef);
        when(userStoreItemJpaRepository.save(any(UserStoreItemEntity.class))).thenReturn(savedEntity);
        when(userStoreItemMapper.toDomain(savedEntity)).thenReturn(UserStoreItem.builder().id(99L).userId(7L).build());

        ArgumentCaptor<UserStoreItemEntity> captor = ArgumentCaptor.forClass(UserStoreItemEntity.class);
        UserStoreItem result = userStoreItemRepository.save(domain);
        verify(userStoreItemJpaRepository).save(captor.capture());
        assertThat(captor.getValue().getUserId()).isEqualTo(7L);
        assertThat(captor.getValue().getStoreItem().getId()).isEqualTo(10L);
        assertThat(captor.getValue().getAcquiredAt()).isEqualTo(now);
        assertThat(result.getId()).isEqualTo(99L);
    }

    @Test 
    void updateEquipped_shouldDelegateToJpaRepository() {
        userStoreItemRepository.updateEquipped(7L, 10L, true);
        verify(userStoreItemJpaRepository).updateEquipped(7L, 10L, true);
    }
    
}
