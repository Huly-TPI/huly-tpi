package com.huly.backend.infrastructure.repository.jpaRepository.implementation;
import com.huly.backend.domain.model.Badge;
import com.huly.backend.domain.model.UserBadge;
import com.huly.backend.infrastructure.repository.entity.UserBadgeEntity;
import com.huly.backend.infrastructure.repository.jpaRepository.interfaces.IBadgeJpaRepository;
import com.huly.backend.infrastructure.repository.jpaRepository.interfaces.IUserBadgeJpaRepository;
import com.huly.backend.infrastructure.repository.mapper.UserBadgeMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import com.huly.backend.infrastructure.repository.entity.BadgeEntity;

import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class UserBadgeRepositoryImplTest {

    @InjectMocks
    private UserBadgeRepositoryImpl userBadgeRepository;

    @Mock
    private IUserBadgeJpaRepository userBadgeJpaRepository;
    @Mock
    private UserBadgeMapper userBadgeMapper;
    @Mock
    private IBadgeJpaRepository iBadgeJpaRepository;

    @Test
    void findAllByUserId_shouldReturnMappedList() {
        UserBadgeEntity entity = UserBadgeEntity.builder().id(1L).build();
        when(userBadgeJpaRepository.findAllByUserId(1L)).thenReturn(List.of(entity));
        when(userBadgeMapper.toDomain(entity)).thenReturn(UserBadge.builder()
                .id(1L)
                .userId(1L)
                .badge(Badge.builder().id(1L).code("PRIMER_PASO").name("Primer paso").build())
                .obtainedAt(entity.getObtainedAt())
                .build());

        List<UserBadge> result = userBadgeRepository.findAllByUserId(1L);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getUserId()).isEqualTo(1L);
        assertThat(result.get(0).getBadge().getCode()).isEqualTo("PRIMER_PASO");
        verify(userBadgeJpaRepository).findAllByUserId(1L);
        verify(userBadgeMapper).toDomain(entity);
    }

    @Test
    void existsByUserIdAndBadgeCode_shouldReturnTrue() {
        when(userBadgeJpaRepository.existsByUserIdAndBadgeCode(1L, "PRIMER_PASO")).thenReturn(true);
        boolean result = userBadgeRepository.existsByUserIdAndBadgeCode(1L, "PRIMER_PASO");
        assertThat(result).isTrue();
        verify(userBadgeJpaRepository).existsByUserIdAndBadgeCode(1L, "PRIMER_PASO");
    }

    @Test
    void existByUserIdAndBageCode_shouldReturnFalse_whenNotExists() {
        when(userBadgeJpaRepository.existsByUserIdAndBadgeCode(1L, "INEXISTENTE")).thenReturn(false);
        boolean result = userBadgeRepository.existsByUserIdAndBadgeCode(1L, "INEXISTENTE");
        assertThat(result).isFalse();
        verify(userBadgeJpaRepository).existsByUserIdAndBadgeCode(1L, "INEXISTENTE");
    }

    @Test
    void save_shouldBuildEntityWithCorrectUserAndBadgeIds(){
        Instant now = Instant.now();
        Badge badge = Badge.builder().id(2L).code("VALENTÍA").name("Valentía").build();
        UserBadge userBadge = UserBadge.builder()
                .id(1L)
                .userId(1L)
                .badge(badge)
                .obtainedAt(now)
                .build();

                UserBadgeEntity savedEntity = UserBadgeEntity.builder().id(10L).build();
                UserBadge mappedResult = UserBadge.builder().id(10L).userId(1L).badge(badge).obtainedAt(now).build();

                ArgumentCaptor<UserBadgeEntity> entityCaptor = ArgumentCaptor.forClass(UserBadgeEntity.class);
                when(userBadgeJpaRepository.save(any(UserBadgeEntity.class))).thenReturn(savedEntity);
                when(userBadgeMapper.toDomain(savedEntity)).thenReturn(mappedResult);
                when(iBadgeJpaRepository.getReferenceById(2L))
            .thenReturn(BadgeEntity.builder().id(2L).code("VALENTÍA").build());

                 UserBadge result = userBadgeRepository.save(userBadge);
                 verify(userBadgeJpaRepository).save(entityCaptor.capture());
                 assertThat(entityCaptor.getValue().getUserId()).isEqualTo(1L);
                 assertThat(entityCaptor.getValue().getBadge().getCode()).isEqualTo("VALENTÍA");
                 assertThat(result.getId()).isEqualTo(10L);
                    assertThat(result.getUserId()).isEqualTo(1L);
                    assertThat(result.getBadge().getCode()).isEqualTo("VALENTÍA");
                    assertThat(result.getObtainedAt()).isEqualTo(now);

    }
    
}
