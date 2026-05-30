package com.huly.backend.infrastructure.repository.jpaRepository.implementation;

import com.huly.backend.domain.model.UserGoal;
import com.huly.backend.domain.model.enums.GoalStatus;
import com.huly.backend.infrastructure.repository.entity.ActivityEntity;
import com.huly.backend.infrastructure.repository.entity.AppUserEntity;
import com.huly.backend.infrastructure.repository.entity.UserGoalsEntity;
import com.huly.backend.infrastructure.repository.jpaRepository.interfaces.IActivityJpaRepository;
import com.huly.backend.infrastructure.repository.jpaRepository.interfaces.AppUserRepository;
import com.huly.backend.infrastructure.repository.jpaRepository.interfaces.IUserGoalJpaRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserGoalRepositoryImplTest {

    @Mock
    private IUserGoalJpaRepository jpaRepository;
    @Mock
    private AppUserRepository appUserRepository;
    @Mock
    private IActivityJpaRepository activityJpaRepository;

    @InjectMocks
    private UserGoalRepositoryImpl repositoryImpl;

    private AppUserEntity userEntity(Long id) {
        AppUserEntity e = new AppUserEntity();
        e.setId(id);
        return e;
    }

    private ActivityEntity activityEntity(Long id) {
        ActivityEntity e = new ActivityEntity();
        e.setId(id);
        return e;
    }

    private UserGoalsEntity savedEntity(Long id, Long userId, Long activityId) {
        ActivityEntity activity = activityId != null ? activityEntity(activityId) : null;
        return UserGoalsEntity.builder()
                .id(id).appUser(userEntity(userId)).title("T").description("D")
                .status(GoalStatus.PENDING).createdAt(Instant.now()).activity(activity)
                .build();
    }

    @Test
    void save_shouldMapDomainToEntityBeforePersisting() {
        UserGoal domain = UserGoal.builder()
                .userId(10L).title("T").description("D").activityId(2L)
                .status(GoalStatus.PENDING).createdAt(Instant.now()).build();

        when(appUserRepository.getReferenceById(10L)).thenReturn(userEntity(10L));
        when(activityJpaRepository.getReferenceById(2L)).thenReturn(activityEntity(2L));
        when(jpaRepository.save(any())).thenReturn(savedEntity(1L, 10L, 2L));

        ArgumentCaptor<UserGoalsEntity> captor = ArgumentCaptor.forClass(UserGoalsEntity.class);
        repositoryImpl.save(domain);

        verify(jpaRepository).save(captor.capture());
        assertThat(captor.getValue().getTitle()).isEqualTo("T");
        assertThat(captor.getValue().getStatus()).isEqualTo(GoalStatus.PENDING);
        assertThat(captor.getValue().getActivity()).isNotNull();
    }

    @Test
    void save_shouldMapEntityToDomainAfterPersisting() {
        UserGoal domain = UserGoal.builder()
                .userId(10L).title("T").activityId(2L)
                .status(GoalStatus.PENDING).createdAt(Instant.now()).build();

        when(appUserRepository.getReferenceById(10L)).thenReturn(userEntity(10L));
        when(activityJpaRepository.getReferenceById(2L)).thenReturn(activityEntity(2L));
        when(jpaRepository.save(any())).thenReturn(savedEntity(1L, 10L, 2L));

        UserGoal result = repositoryImpl.save(domain);

        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getUserId()).isEqualTo(10L);
        assertThat(result.getActivityId()).isEqualTo(2L);
    }

    @Test
    void save_shouldHandleNullActivityId() {
        UserGoal domain = UserGoal.builder()
                .userId(10L).title("T").activityId(null)
                .status(GoalStatus.PENDING).createdAt(Instant.now()).build();

        when(appUserRepository.getReferenceById(10L)).thenReturn(userEntity(10L));
        when(jpaRepository.save(any())).thenReturn(savedEntity(1L, 10L, null));

        UserGoal result = repositoryImpl.save(domain);

        verify(activityJpaRepository, never()).getReferenceById(any());
        assertThat(result.getActivityId()).isNull();
    }

    @Test
    void findByUserIdAndStatus_shouldReturnMappedPage() {
        PageRequest pageable = PageRequest.of(0, 5);
        Page<UserGoalsEntity> entityPage = new PageImpl<>(List.of(savedEntity(1L, 5L, null)));
        when(jpaRepository.findByAppUser_IdAndStatus(5L, GoalStatus.PENDING, pageable)).thenReturn(entityPage);

        Page<UserGoal> result = repositoryImpl.findByUserIdAndStatus(5L, GoalStatus.PENDING, pageable);

        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getUserId()).isEqualTo(5L);
        assertThat(result.getContent().get(0).getStatus()).isEqualTo(GoalStatus.PENDING);
    }

    @Test
    void findByUserIdAndStatus_shouldReturnEmptyPage_whenNoneFound() {
        PageRequest pageable = PageRequest.of(0, 5);
        when(jpaRepository.findByAppUser_IdAndStatus(99L, GoalStatus.COMPLETED, pageable))
                .thenReturn(Page.empty(pageable));

        Page<UserGoal> result = repositoryImpl.findByUserIdAndStatus(99L, GoalStatus.COMPLETED, pageable);

        assertThat(result.getContent()).isEmpty();
    }

    @Test
    void findById_shouldReturnMappedGoal_whenExists() {
        when(jpaRepository.findById(1L)).thenReturn(Optional.of(savedEntity(1L, 10L, 2L)));

        Optional<UserGoal> result = repositoryImpl.findById(1L);

        assertThat(result).isPresent();
        assertThat(result.get().getId()).isEqualTo(1L);
        assertThat(result.get().getActivityId()).isEqualTo(2L);
    }

    @Test
    void findById_shouldReturnEmpty_whenNotFound() {
        when(jpaRepository.findById(99L)).thenReturn(Optional.empty());

        assertThat(repositoryImpl.findById(99L)).isEmpty();
    }

    @Test
    void existsById_shouldReturnTrue_whenFound() {
        when(jpaRepository.existsById(1L)).thenReturn(true);
        assertThat(repositoryImpl.existsById(1L)).isTrue();
    }

    @Test
    void existsById_shouldReturnFalse_whenNotFound() {
        when(jpaRepository.existsById(99L)).thenReturn(false);
        assertThat(repositoryImpl.existsById(99L)).isFalse();
    }

    @Test
    void deleteById_shouldDelegateToJpa() {
        repositoryImpl.deleteById(1L);
        verify(jpaRepository).deleteById(1L);
    }
}
