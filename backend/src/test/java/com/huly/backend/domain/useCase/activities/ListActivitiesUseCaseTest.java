package com.huly.backend.domain.useCase.activities;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.huly.backend.domain.dto.activities.ListActivitiesResponse;
import com.huly.backend.domain.mapper.activities.ListActivitiesMapper;
import com.huly.backend.domain.model.activity.Activity;
import com.huly.backend.domain.model.enums.ActivityType;
import com.huly.backend.domain.repository.activity.ActivityRepository;
import java.util.List;

@ExtendWith(MockitoExtension.class)
class ListActivitiesUseCaseTest {

    @Mock
    private ActivityRepository activityRepository;

    private ListActivitiesUseCase listActivitiesUseCase;

    @BeforeEach
    void setUp() {
        listActivitiesUseCase = new ListActivitiesUseCase(activityRepository, new ListActivitiesMapper());
    }

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

        ListActivitiesResponse result = listActivitiesUseCase.execute();
        assertThat(result.activities()).hasSize(1);
        assertThat(result.activities().get(0).type()).isEqualTo(ActivityType.RESPIRACION);
        verify(activityRepository).findAll();
        }

        @Test
        void execute_shouldReturnEmptyListWhenNoActivities() {
            when(activityRepository.findAll()).thenReturn(List.of());

            ListActivitiesResponse result = listActivitiesUseCase.execute();
            assertThat(result.activities()).isEmpty();
            verify(activityRepository).findAll();
        }


}
