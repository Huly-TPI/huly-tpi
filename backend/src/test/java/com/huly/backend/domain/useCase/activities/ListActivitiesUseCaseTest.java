package com.huly.backend.domain.useCase.activities;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.huly.backend.domain.dto.activities.ListActivitiesResponse;
import com.huly.backend.domain.mapper.activities.ListActivitiesMapper;
import com.huly.backend.domain.model.activity.Activity;
import com.huly.backend.domain.model.enums.ActivityType;
import com.huly.backend.domain.repository.activity.ActivityRepository;

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
    @DisplayName("Devuelve todas las actividades mapeadas cuando el repositorio tiene datos")
    void listShouldReturnAllActivities() {
        // --- arrange ---
        givenStoredActivities(List.of(breathingActivity()));

        // --- act ---
        ListActivitiesResponse result = list();

        // --- assert ---
        thenSingleActivityOfTypeReturned(result, ActivityType.BREATHING);
        thenActivitiesWereQueried();
    }

    @Test
    @DisplayName("Devuelve una lista vacía cuando el repositorio no tiene actividades")
    void listShouldReturnEmptyListWhenNoActivities() {
        // --- arrange ---
        givenStoredActivities(List.of());

        // --- act ---
        ListActivitiesResponse result = list();

        // --- assert ---
        thenNoActivitiesReturned(result);
        thenActivitiesWereQueried();
    }

    // --- arrange ---

    private void givenStoredActivities(List<Activity> activities) {
        when(activityRepository.findAll()).thenReturn(activities);
    }

    private Activity breathingActivity() {
        return Activity.builder()
                .id(1L)
                .type(ActivityType.BREATHING)
                .valenceMin(-1.0).valenceMax(1.0)
                .arousalMin(-1.0).arousalMax(1.0)
                .dominanceMin(-1.0).dominanceMax(1.0)
                .effectValence(0.3).effectArousal(0.2).effectDominance(0.1)
                .build();
    }

    // --- act ---

    private ListActivitiesResponse list() {
        return listActivitiesUseCase.execute();
    }

    // --- assert ---

    private void thenSingleActivityOfTypeReturned(ListActivitiesResponse result, ActivityType expectedType) {
        assertThat(result.activities()).hasSize(1);
        assertThat(result.activities().get(0).type()).isEqualTo(expectedType);
    }

    private void thenNoActivitiesReturned(ListActivitiesResponse result) {
        assertThat(result.activities()).isEmpty();
    }

    private void thenActivitiesWereQueried() {
        verify(activityRepository).findAll();
    }
}
