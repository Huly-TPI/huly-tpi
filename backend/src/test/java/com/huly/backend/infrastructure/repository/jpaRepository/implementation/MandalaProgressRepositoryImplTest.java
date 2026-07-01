package com.huly.backend.infrastructure.repository.jpaRepository.implementation;

import com.huly.backend.domain.model.mandala.MandalaProgress;
import com.huly.backend.infrastructure.repository.entity.MandalaProgressEntity;
import com.huly.backend.infrastructure.repository.jpaRepository.interfaces.IMandalaProgressJpaRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class MandalaProgressRepositoryImplTest {

    @Mock
    private IMandalaProgressJpaRepository jpaRepository;

    @InjectMocks
    private MandalaProgressRepositoryImpl repository;

    @Test
    void save_newProgress_createsAndSavesEntity() {
        MandalaProgress progress = MandalaProgress.builder()
                .userId(7L)
                .mandalaId("mandala-01")
                .paintBlob("paint".getBytes())
                .sessionRegistered(true)
                .build();

        when(jpaRepository.findByUserIdAndMandalaId(7L, "mandala-01")).thenReturn(Optional.empty());
        MandalaProgressEntity savedEntity = MandalaProgressEntity.builder()
                .userId(7L)
                .mandalaId("mandala-01")
                .paintBlob("paint".getBytes())
                .sessionRegistered(true)
                .build();
        when(jpaRepository.save(any(MandalaProgressEntity.class))).thenReturn(savedEntity);

        MandalaProgress result = repository.save(progress);

        ArgumentCaptor<MandalaProgressEntity> captor = ArgumentCaptor.forClass(MandalaProgressEntity.class);
        verify(jpaRepository).save(captor.capture());
        assertThat(captor.getValue().getUserId()).isEqualTo(7L);
        assertThat(captor.getValue().getMandalaId()).isEqualTo("mandala-01");
        assertThat(captor.getValue().getPaintBlob()).isEqualTo("paint".getBytes());
        assertThat(captor.getValue().isSessionRegistered()).isTrue();

        assertThat(result.getUserId()).isEqualTo(7L);
        assertThat(result.getMandalaId()).isEqualTo("mandala-01");
    }

    @Test
    void save_existingProgress_updatesAndSavesEntity() {
        MandalaProgress progress = MandalaProgress.builder()
                .userId(7L)
                .mandalaId("mandala-01")
                .paintBlob("new-paint".getBytes())
                .sessionRegistered(true)
                .build();

        MandalaProgressEntity existingEntity = MandalaProgressEntity.builder()
                .userId(7L)
                .mandalaId("mandala-01")
                .paintBlob("old-paint".getBytes())
                .sessionRegistered(false)
                .build();

        when(jpaRepository.findByUserIdAndMandalaId(7L, "mandala-01")).thenReturn(Optional.of(existingEntity));
        when(jpaRepository.save(any(MandalaProgressEntity.class))).thenReturn(existingEntity);

        repository.save(progress);

        verify(jpaRepository).save(existingEntity);
        assertThat(existingEntity.getPaintBlob()).isEqualTo("new-paint".getBytes());
    }

    @Test
    void findByUserIdAndMandalaId_shouldReturnMappedDomain() {
        MandalaProgressEntity entity = MandalaProgressEntity.builder()
                .userId(7L)
                .mandalaId("mandala-01")
                .paintBlob("paint".getBytes())
                .sessionRegistered(true)
                .build();

        when(jpaRepository.findByUserIdAndMandalaId(7L, "mandala-01")).thenReturn(Optional.of(entity));

        Optional<MandalaProgress> result = repository.findByUserIdAndMandalaId(7L, "mandala-01");

        assertThat(result).isPresent();
        assertThat(result.get().getUserId()).isEqualTo(7L);
        assertThat(result.get().getMandalaId()).isEqualTo("mandala-01");
        assertThat(result.get().getPaintBlob()).isEqualTo("paint".getBytes());
        assertThat(result.get().isSessionRegistered()).isTrue();
    }

    @Test
    void markSessionRegistered_existingProgress_updatesFlag() {
        MandalaProgressEntity entity = MandalaProgressEntity.builder()
                .userId(7L)
                .mandalaId("mandala-01")
                .sessionRegistered(false)
                .build();

        when(jpaRepository.findByUserIdAndMandalaId(7L, "mandala-01")).thenReturn(Optional.of(entity));

        repository.markSessionRegistered(7L, "mandala-01");

        verify(jpaRepository).save(entity);
        assertThat(entity.isSessionRegistered()).isTrue();
    }

    @Test
    void deleteByUserIdAndMandalaId_shouldDelegateToJpaRepository() {
        repository.deleteByUserIdAndMandalaId(7L, "mandala-01");
        verify(jpaRepository).deleteByUserIdAndMandalaId(7L, "mandala-01");
    }
}
