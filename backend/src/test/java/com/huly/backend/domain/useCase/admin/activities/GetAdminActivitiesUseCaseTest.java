package com.huly.backend.domain.useCase.admin.activities;

import com.huly.backend.domain.model.activity.Activity;
import com.huly.backend.domain.repository.activity.ActivityRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GetAdminActivitiesUseCaseTest {

    private static final Activity ACTIVITY_1 = Activity.builder().id(1L).title("Act1").build();
    private static final Activity ACTIVITY_2 = Activity.builder().id(2L).title("Act2").build();

    @Mock
    private ActivityRepository activityRepository;

    @InjectMocks
    private GetAdminActivitiesUseCase useCase;

    @Test
    @DisplayName("Devuelve todas las actividades del repositorio en orden")
    void executeShouldReturnAllActivities() {
        // --- arrange ---
        givenStoredActivities();
        // --- act ---
        List<Activity> result = fetchActivities();
        // --- assert ---
        thenActivitiesAreReturnedInOrder(result);
    }

    // --- arrange ---

    private void givenStoredActivities() {
        when(activityRepository.findAll()).thenReturn(List.of(ACTIVITY_1, ACTIVITY_2));
    }

    // --- act ---

    private List<Activity> fetchActivities() {
        return useCase.execute();
    }

    // --- assert ---

    private void thenActivitiesAreReturnedInOrder(List<Activity> result) {
        assertThat(result).hasSize(2).containsExactly(ACTIVITY_1, ACTIVITY_2);
    }
}
