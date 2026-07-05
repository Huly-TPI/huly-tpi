package com.huly.backend.domain.useCase.admin.activities;

import com.huly.backend.domain.dto.admin.activities.UpdateActivityConfigRequest;
import com.huly.backend.domain.exception.BusinessRuleException;
import com.huly.backend.domain.exception.ResourceNotFoundException;
import com.huly.backend.domain.mapper.activities.UpdateActivityConfigMapper;
import com.huly.backend.domain.model.activity.Activity;
import com.huly.backend.domain.model.enums.ActivityType;
import com.huly.backend.domain.repository.activity.ActivityRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class UpdateActivityConfigUseCaseTest {

    private ActivityRepository activityRepository;
    private UpdateActivityConfigMapper mapper;
    private UpdateActivityConfigUseCase useCase;

    @BeforeEach
    void setUp() {
        activityRepository = mock(ActivityRepository.class);
        mapper = new UpdateActivityConfigMapper();
        useCase = new UpdateActivityConfigUseCase(activityRepository, mapper);
    }

    @Test
    void execute_shouldThrowException_whenActivityNotFound() {
        when(activityRepository.findById(99L)).thenReturn(Optional.empty());

        UpdateActivityConfigRequest request = new UpdateActivityConfigRequest(
                -0.5, 0.5, -0.5, 0.5, -0.5, 0.5, 0.1, 0.1, 0.1, "Titulo", "Desc", "key", "/path"
        );

        assertThatThrownBy(() -> useCase.execute(99L, request))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("99");
    }

    @Test
    void execute_shouldUpdateAndSave_whenRequestIsValid() {
        Activity existing = Activity.builder()
                .id(1L)
                .type(ActivityType.BREATHING)
                .title("Original")
                .build();
        when(activityRepository.findById(1L)).thenReturn(Optional.of(existing));

        UpdateActivityConfigRequest request = new UpdateActivityConfigRequest(
                -0.5, 0.5, -0.5, 0.5, -0.5, 0.5, 0.1, -0.2, 0.3, "Nuevo Titulo", "Nueva Desc", "keyword", "/new-path"
        );

        when(activityRepository.save(any(Activity.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Activity result = useCase.execute(1L, request);

        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getType()).isEqualTo(ActivityType.BREATHING);
        assertThat(result.getValenceMin()).isEqualTo(-0.5);
        assertThat(result.getValenceMax()).isEqualTo(0.5);
        assertThat(result.getArousalMin()).isEqualTo(-0.5);
        assertThat(result.getArousalMax()).isEqualTo(0.5);
        assertThat(result.getDominanceMin()).isEqualTo(-0.5);
        assertThat(result.getDominanceMax()).isEqualTo(0.5);
        assertThat(result.getEffectValence()).isEqualTo(0.1);
        assertThat(result.getEffectArousal()).isEqualTo(-0.2);
        assertThat(result.getEffectDominance()).isEqualTo(0.3);
        assertThat(result.getTitle()).isEqualTo("Nuevo Titulo");
        assertThat(result.getDescription()).isEqualTo("Nueva Desc");
        assertThat(result.getGoalKeywords()).isEqualTo("keyword");
        assertThat(result.getRoutePath()).isEqualTo("/new-path");
    }

    @Test
    void validate_shouldThrowException_whenRangeIsInvalid() {
        Activity existing = Activity.builder().id(1L).build();
        when(activityRepository.findById(1L)).thenReturn(Optional.of(existing));

        // Invalid valenceMin (< -1.0)
        assertValidationError(new UpdateActivityConfigRequest(
                -1.5, 0.5, -0.5, 0.5, -0.5, 0.5, 0.1, 0.1, 0.1, "T", "D", "K", "/p"
        ), "valenceMin debe estar entre -1.0 y 1.0");

        // Invalid valenceMax (> 1.0)
        assertValidationError(new UpdateActivityConfigRequest(
                -0.5, 1.5, -0.5, 0.5, -0.5, 0.5, 0.1, 0.1, 0.1, "T", "D", "K", "/p"
        ), "valenceMax debe estar entre -1.0 y 1.0");

        // Invalid arousalMin (< -1.0)
        assertValidationError(new UpdateActivityConfigRequest(
                -0.5, 0.5, -2.0, 0.5, -0.5, 0.5, 0.1, 0.1, 0.1, "T", "D", "K", "/p"
        ), "arousalMin debe estar entre -1.0 y 1.0");

        // Invalid arousalMax (> 1.0)
        assertValidationError(new UpdateActivityConfigRequest(
                -0.5, 0.5, -0.5, 2.5, -0.5, 0.5, 0.1, 0.1, 0.1, "T", "D", "K", "/p"
        ), "arousalMax debe estar entre -1.0 y 1.0");

        // Invalid dominanceMin (< -1.0)
        assertValidationError(new UpdateActivityConfigRequest(
                -0.5, 0.5, -0.5, 0.5, -1.1, 0.5, 0.1, 0.1, 0.1, "T", "D", "K", "/p"
        ), "dominanceMin debe estar entre -1.0 y 1.0");

        // Invalid dominanceMax (> 1.0)
        assertValidationError(new UpdateActivityConfigRequest(
                -0.5, 0.5, -0.5, 0.5, -0.5, 1.01, 0.1, 0.1, 0.1, "T", "D", "K", "/p"
        ), "dominanceMax debe estar entre -1.0 y 1.0");

        // Invalid effectValence (< -1.0)
        assertValidationError(new UpdateActivityConfigRequest(
                -0.5, 0.5, -0.5, 0.5, -0.5, 0.5, -1.05, 0.1, 0.1, "T", "D", "K", "/p"
        ), "effectValence debe estar entre -1.0 y 1.0");

        // Invalid effectArousal (> 1.0)
        assertValidationError(new UpdateActivityConfigRequest(
                -0.5, 0.5, -0.5, 0.5, -0.5, 0.5, 0.1, 1.05, 0.1, "T", "D", "K", "/p"
        ), "effectArousal debe estar entre -1.0 y 1.0");

        // Invalid effectDominance (< -1.0)
        assertValidationError(new UpdateActivityConfigRequest(
                -0.5, 0.5, -0.5, 0.5, -0.5, 0.5, 0.1, 0.1, -1.01, "T", "D", "K", "/p"
        ), "effectDominance debe estar entre -1.0 y 1.0");
    }

    @Test
    void validate_shouldThrowException_whenMinIsGreaterThanMax() {
        Activity existing = Activity.builder().id(1L).build();
        when(activityRepository.findById(1L)).thenReturn(Optional.of(existing));

        // valenceMin > valenceMax
        assertValidationError(new UpdateActivityConfigRequest(
                0.6, 0.5, -0.5, 0.5, -0.5, 0.5, 0.1, 0.1, 0.1, "T", "D", "K", "/p"
        ), "valenceMin no puede ser mayor que valenceMax");

        // arousalMin > arousalMax
        assertValidationError(new UpdateActivityConfigRequest(
                -0.5, 0.5, 0.8, 0.5, -0.5, 0.5, 0.1, 0.1, 0.1, "T", "D", "K", "/p"
        ), "arousalMin no puede ser mayor que arousalMax");

        // dominanceMin > dominanceMax
        assertValidationError(new UpdateActivityConfigRequest(
                -0.5, 0.5, -0.5, 0.5, 0.9, 0.5, 0.1, 0.1, 0.1, "T", "D", "K", "/p"
        ), "dominanceMin no puede ser mayor que dominanceMax");
    }

    @Test
    void validate_shouldThrowException_whenRequiredFieldIsBlank() {
        Activity existing = Activity.builder().id(1L).build();
        when(activityRepository.findById(1L)).thenReturn(Optional.of(existing));

        // null title
        assertValidationError(new UpdateActivityConfigRequest(
                -0.5, 0.5, -0.5, 0.5, -0.5, 0.5, 0.1, 0.1, 0.1, null, "D", "K", "/p"
        ), "El título es obligatorio");

        // empty title
        assertValidationError(new UpdateActivityConfigRequest(
                -0.5, 0.5, -0.5, 0.5, -0.5, 0.5, 0.1, 0.1, 0.1, "  ", "D", "K", "/p"
        ), "El título es obligatorio");

        // null description
        assertValidationError(new UpdateActivityConfigRequest(
                -0.5, 0.5, -0.5, 0.5, -0.5, 0.5, 0.1, 0.1, 0.1, "T", null, "K", "/p"
        ), "La descripción es obligatoria");

        // empty description
        assertValidationError(new UpdateActivityConfigRequest(
                -0.5, 0.5, -0.5, 0.5, -0.5, 0.5, 0.1, 0.1, 0.1, "T", "", "K", "/p"
        ), "La descripción es obligatoria");

        // null routePath
        assertValidationError(new UpdateActivityConfigRequest(
                -0.5, 0.5, -0.5, 0.5, -0.5, 0.5, 0.1, 0.1, 0.1, "T", "D", "K", null
        ), "La ruta de navegación es obligatoria");

        // empty routePath
        assertValidationError(new UpdateActivityConfigRequest(
                -0.5, 0.5, -0.5, 0.5, -0.5, 0.5, 0.1, 0.1, 0.1, "T", "D", "K", "  "
        ), "La ruta de navegación es obligatoria");
    }

    private void assertValidationError(UpdateActivityConfigRequest request, String expectedMessage) {
        assertThatThrownBy(() -> useCase.execute(1L, request))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessage(expectedMessage);
    }
}
