package com.huly.backend.infrastructure.repository.jpaRepository.implementation;

import com.huly.backend.domain.model.LanternThought;
import com.huly.backend.domain.model.enums.LanternStatus;
import com.huly.backend.infrastructure.repository.entity.AppUserEntity;
import com.huly.backend.infrastructure.repository.entity.LanternThoughtEntity;
import com.huly.backend.infrastructure.repository.jpaRepository.interfaces.AppUserRepository;
import com.huly.backend.infrastructure.repository.jpaRepository.interfaces.ILanternThoughtJpaRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LanternThoughtRepositoryImplTest {

    @Mock
    private ILanternThoughtJpaRepository jpaRepository;

    @Mock
    private AppUserRepository appUserRepository;

    @InjectMocks
    private LanternThoughtRepositoryImpl repositoryImpl;

    private AppUserEntity userEntity(Long userId) {
        return AppUserEntity.builder().id(userId).build();
    }

    private LanternThoughtEntity thoughtEntity(Long id, AppUserEntity user, String text, LanternStatus status, boolean workedOn) {
        return LanternThoughtEntity.builder()
                .id(id).user(user).text(text).status(status)
                .workedOn(workedOn).createdAt(Instant.parse("2025-01-01T00:00:00Z")).build();
    }

    @Test
    void save_shouldPersistEntityWithActiveStatusAndWorkedOnFalse() {
        AppUserEntity user = userEntity(10L);
        LanternThoughtEntity saved = thoughtEntity(1L, user, "pensamiento", LanternStatus.ACTIVE, false);
        when(appUserRepository.getReferenceById(10L)).thenReturn(user);
        when(jpaRepository.save(any(LanternThoughtEntity.class))).thenReturn(saved);

        ArgumentCaptor<LanternThoughtEntity> captor = ArgumentCaptor.forClass(LanternThoughtEntity.class);
        repositoryImpl.save(10L, "pensamiento");

        verify(jpaRepository).save(captor.capture());
        LanternThoughtEntity captured = captor.getValue();
        assertThat(captured.getText()).isEqualTo("pensamiento");
        assertThat(captured.getStatus()).isEqualTo(LanternStatus.ACTIVE);
        assertThat(captured.isWorkedOn()).isFalse();
    }

    @Test
    void save_shouldReturnMappedDomain() {
        AppUserEntity user = userEntity(10L);
        LanternThoughtEntity saved = thoughtEntity(1L, user, "pensamiento", LanternStatus.ACTIVE, false);
        when(appUserRepository.getReferenceById(10L)).thenReturn(user);
        when(jpaRepository.save(any(LanternThoughtEntity.class))).thenReturn(saved);

        LanternThought result = repositoryImpl.save(10L, "pensamiento");

        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getUserId()).isEqualTo(10L);
        assertThat(result.getText()).isEqualTo("pensamiento");
        assertThat(result.getStatus()).isEqualTo(LanternStatus.ACTIVE);
        assertThat(result.isWorkedOn()).isFalse();
    }

    @Test
    void findAllByUserId_shouldReturnOnlyActiveThoughts() {
        AppUserEntity user = userEntity(5L);
        List<LanternThoughtEntity> entities = List.of(
                thoughtEntity(1L, user, "pensamiento 1", LanternStatus.ACTIVE, false),
                thoughtEntity(2L, user, "pensamiento 2", LanternStatus.ACTIVE, true)
        );
        when(jpaRepository.findAllByUser_IdAndStatusOrderByCreatedAtDesc(5L, LanternStatus.ACTIVE)).thenReturn(entities);

        List<LanternThought> result = repositoryImpl.findAllByUserId(5L);

        assertThat(result).hasSize(2);
        assertThat(result.get(0).getText()).isEqualTo("pensamiento 1");
        assertThat(result.get(1).getText()).isEqualTo("pensamiento 2");
    }

    @Test
    void findAllByUserId_shouldReturnEmptyList_whenNoActiveThoughts() {
        when(jpaRepository.findAllByUser_IdAndStatusOrderByCreatedAtDesc(99L, LanternStatus.ACTIVE)).thenReturn(List.of());

        List<LanternThought> result = repositoryImpl.findAllByUserId(99L);

        assertThat(result).isEmpty();
    }

    @Test
    void findByIdAndUserId_shouldReturnMappedDomain_whenEntityExists() {
        AppUserEntity user = userEntity(10L);
        LanternThoughtEntity entity = thoughtEntity(1L, user, "pensamiento", LanternStatus.ACTIVE, false);
        when(jpaRepository.findByIdAndUser_Id(1L, 10L)).thenReturn(Optional.of(entity));

        Optional<LanternThought> result = repositoryImpl.findByIdAndUserId(1L, 10L);

        assertThat(result).isPresent();
        assertThat(result.get().getId()).isEqualTo(1L);
        assertThat(result.get().getUserId()).isEqualTo(10L);
    }

    @Test
    void findByIdAndUserId_shouldReturnEmpty_whenEntityDoesNotExist() {
        when(jpaRepository.findByIdAndUser_Id(99L, 10L)).thenReturn(Optional.empty());

        Optional<LanternThought> result = repositoryImpl.findByIdAndUserId(99L, 10L);

        assertThat(result).isEmpty();
    }

    @Test
    void updateStatus_shouldSetNewStatusAndSave() {
        AppUserEntity user = userEntity(10L);
        LanternThoughtEntity entity = thoughtEntity(1L, user, "pensamiento", LanternStatus.ACTIVE, false);
        LanternThoughtEntity updated = thoughtEntity(1L, user, "pensamiento", LanternStatus.COMPLETED, false);
        when(jpaRepository.findById(1L)).thenReturn(Optional.of(entity));
        when(jpaRepository.save(entity)).thenReturn(updated);

        LanternThought result = repositoryImpl.updateStatus(1L, LanternStatus.COMPLETED);

        assertThat(entity.getStatus()).isEqualTo(LanternStatus.COMPLETED);
        assertThat(result.getStatus()).isEqualTo(LanternStatus.COMPLETED);
    }

    @Test
    void updateStatus_shouldThrow_whenEntityNotFound() {
        when(jpaRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> repositoryImpl.updateStatus(99L, LanternStatus.COMPLETED))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("99");
    }

    @Test
    void markWorkedOn_shouldSetWorkedOnTrueAndSave() {
        AppUserEntity user = userEntity(10L);
        LanternThoughtEntity entity = thoughtEntity(1L, user, "pensamiento", LanternStatus.ACTIVE, false);
        when(jpaRepository.findById(1L)).thenReturn(Optional.of(entity));
        when(jpaRepository.save(entity)).thenReturn(entity);

        repositoryImpl.markWorkedOn(1L);

        assertThat(entity.isWorkedOn()).isTrue();
        verify(jpaRepository).save(entity);
    }

    @Test
    void markWorkedOn_shouldThrow_whenEntityNotFound() {
        when(jpaRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> repositoryImpl.markWorkedOn(99L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("99");
    }
}
