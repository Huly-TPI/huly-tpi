package com.huly.backend.infrastructure.repository.jpaRepository.implementation;

import com.huly.backend.domain.model.activity.Activity;
import com.huly.backend.domain.model.enums.ActivityType;
import com.huly.backend.infrastructure.repository.entity.ActivityEntity;
import com.huly.backend.infrastructure.repository.jpaRepository.interfaces.IActivityJpaRepository;
import com.huly.backend.infrastructure.repository.mapper.ActivityMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ActivityRepositoryImplTest {

    @Mock
    private IActivityJpaRepository jpaRepository;

    @Spy
    private ActivityMapper activityMapper = new ActivityMapper();

    private ActivityRepositoryImpl repository;

    @BeforeEach
    void setUp() {
        repository = new ActivityRepositoryImpl(jpaRepository, activityMapper);
    }

    @Test
    void findAll_shouldReturnMappedDomainList() {
        ActivityEntity entity1 = ActivityEntity.builder().id(1L).type(ActivityType.BREATHING).title("Respirar").build();
        ActivityEntity entity2 = ActivityEntity.builder().id(2L).type(ActivityType.DIARY).title("Diario").build();

        when(jpaRepository.findAll()).thenReturn(List.of(entity1, entity2));

        List<Activity> result = repository.findAll();

        assertThat(result).hasSize(2);
        assertThat(result.get(0).getTitle()).isEqualTo("Respirar");
        assertThat(result.get(1).getTitle()).isEqualTo("Diario");
        verify(jpaRepository).findAll();
    }

    @Test
    void existsById_shouldCallJpaRepository() {
        when(jpaRepository.existsById(1L)).thenReturn(true);
        when(jpaRepository.existsById(2L)).thenReturn(false);

        assertThat(repository.existsById(1L)).isTrue();
        assertThat(repository.existsById(2L)).isFalse();
        verify(jpaRepository).existsById(1L);
        verify(jpaRepository).existsById(2L);
    }

    @Test
    void findById_shouldReturnOptionalDomain() {
        ActivityEntity entity = ActivityEntity.builder().id(1L).type(ActivityType.BREATHING).title("Respirar").build();
        when(jpaRepository.findById(1L)).thenReturn(Optional.of(entity));
        when(jpaRepository.findById(2L)).thenReturn(Optional.empty());

        Optional<Activity> act1 = repository.findById(1L);
        Optional<Activity> act2 = repository.findById(2L);

        assertThat(act1).isPresent();
        assertThat(act1.get().getTitle()).isEqualTo("Respirar");
        assertThat(act2).isEmpty();

        verify(jpaRepository).findById(1L);
        verify(jpaRepository).findById(2L);
    }

    @Test
    void save_shouldMapAndSaveAndReturnDomain() {
        Activity domain = Activity.builder().id(1L).type(ActivityType.BREATHING).title("Respirar").build();
        ActivityEntity entity = ActivityEntity.builder().id(1L).type(ActivityType.BREATHING).title("Respirar").build();

        when(jpaRepository.save(any(ActivityEntity.class))).thenReturn(entity);

        Activity saved = repository.save(domain);

        assertThat(saved.getTitle()).isEqualTo("Respirar");
        verify(jpaRepository).save(any(ActivityEntity.class));
    }
}
