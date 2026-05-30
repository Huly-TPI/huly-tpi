package com.huly.backend.infrastructure.repository.jpaRepository.implementation;
import com.huly.backend.domain.model.Activity;
import com.huly.backend.domain.model.enums.ActivityType;
import com.huly.backend.infrastructure.repository.entity.ActivityEntity;
import com.huly.backend.infrastructure.repository.jpaRepository.interfaces.IActivityJpaRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import java.util.List;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


@ExtendWith(MockitoExtension.class)
public class ActivityRepositoryImplTest {
    

    @Mock
    private IActivityJpaRepository activityJpaRepository;

    @InjectMocks
    private ActivityRepositoryImpl activityRepository;

    @Test
    void findAll_shouldReturnMappedDomainList() { 
        ActivityEntity entity = ActivityEntity.builder() 
        .id(1L)
        .type(ActivityType.RESPIRACION)
        .valenceMin(-1.0).valenceMax(1.0)
        .arousalMin(-1.0).arousalMax(1.0)
        .dominanceMin(-1.0).dominanceMax(1.0)
        .effectValence(0.3).effectArousal(0.2).effectDominance(0.1)
        .build();

        when(activityJpaRepository.findAll()).thenReturn(List.of(entity));
        
        List<Activity> result = activityRepository.findAll();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getId()).isEqualTo(1L);
        assertThat(result.get(0).getType()).isEqualTo(ActivityType.RESPIRACION);
        assertThat(result.get(0).getValenceMin()).isEqualTo(-1.0);
        verify(activityJpaRepository).findAll();
    }

    @Test
    void findAll_shouldReturnEmptyList_whenNoEntities() {
        when(activityJpaRepository.findAll()).thenReturn(List.of());

        List<Activity> result = activityRepository.findAll();

        assertThat(result).isEmpty();
        verify(activityJpaRepository).findAll();
    }
}
