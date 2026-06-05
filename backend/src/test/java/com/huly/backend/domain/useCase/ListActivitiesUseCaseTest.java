package com.huly.backend.domain.useCase;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.assertj.core.api.Assertions.assertThat;

import com.huly.backend.domain.useCase.activities.ListActivitiesUseCase;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.huly.backend.domain.model.Activity;
import com.huly.backend.domain.model.enums.ActivityType;
import com.huly.backend.domain.repository.ActivityRepository;
import java.util.List;

@ExtendWith(MockitoExtension.class)
class ListActivitiesUseCaseTest {
    


    @Mock
    private ActivityRepository activityRepository;

    @InjectMocks
    private ListActivitiesUseCase listActivitiesUseCase;

    @Test
    void execute_shouldReturnAllActivities() {
        List<Activity> activities = List.of( 
            Activity.builder()
            .id(1L)
            .type(ActivityType.RESPIRACION)
            .valenceMin(-1.0).valenceMax(1.0)
            .arousalMin(-1.0).arousalMax(1.0)
            .dominanceMin(-1.0).dominanceMax(1.0)
            .effectValence(0.3).effectArousal(0.2).effectDominance(0.1)
            .build()
        );
        when(activityRepository.findAll()).thenReturn(activities);

        List<Activity> result = listActivitiesUseCase.execute();
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getType()).isEqualTo(ActivityType.RESPIRACION);
        verify(activityRepository).findAll();
        }

        @Test
        void execute_shouldReturnEmptyListWhenNoActivities() {
            when(activityRepository.findAll()).thenReturn(List.of());

            List<Activity> result = listActivitiesUseCase.execute();
            assertThat(result).isEmpty();
            verify(activityRepository).findAll();
        }


}
