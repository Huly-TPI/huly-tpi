package com.huly.backend.domain.useCase.admin.activities;

import com.huly.backend.domain.dto.admin.activities.UpdateActivityConfigRequest;
import com.huly.backend.domain.exception.BusinessRuleException;
import com.huly.backend.domain.exception.ResourceNotFoundException;
import com.huly.backend.domain.mapper.activities.UpdateActivityConfigMapper;
import com.huly.backend.domain.model.activity.Activity;
import com.huly.backend.domain.model.enums.ActivityType;
import com.huly.backend.domain.repository.activity.ActivityRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UpdateActivityConfigUseCaseTest {

    private static final Long ACTIVITY_ID = 1L;
    private static final Long MISSING_ID = 99L;

    @Mock
    private ActivityRepository activityRepository;

    private UpdateActivityConfigMapper mapper;
    private UpdateActivityConfigUseCase useCase;

    @BeforeEach
    void setUp() {
        mapper = new UpdateActivityConfigMapper();
        useCase = new UpdateActivityConfigUseCase(activityRepository, mapper);
    }

    @Test
    @DisplayName("Actualiza y persiste la actividad cuando la solicitud es válida")
    void executeShouldUpdateAndSaveWhenRequestIsValid() {
        // --- arrange ---
        givenExistingActivity();
        givenRepositoryEchoesSavedActivity();
        // --- act ---
        Activity result = updateWithValidRequest();
        // --- assert ---
        thenUpdatedActivityMatchesRequest(result);
    }

    @Test
    @DisplayName("Lanza ResourceNotFound cuando la actividad no existe")
    void executeShouldThrowWhenActivityNotFound() {
        // --- arrange ---
        givenActivityNotFound();
        // --- assert ---
        thenUpdateThrowsResourceNotFound();
    }

    @Test
    @DisplayName("Lanza error de negocio cuando algún rango está fuera de [-1.0, 1.0]")
    void executeShouldThrowWhenRangeIsInvalid() {
        // --- arrange ---
        givenExistingActivity();
        // --- assert ---
        thenEachOutOfRangeFieldThrows();
    }

    @Test
    @DisplayName("Lanza error de negocio cuando un mínimo es mayor que su máximo")
    void executeShouldThrowWhenMinIsGreaterThanMax() {
        // --- arrange ---
        givenExistingActivity();
        // --- assert ---
        thenEachMinGreaterThanMaxThrows();
    }

    @Test
    @DisplayName("Lanza error de negocio cuando un campo obligatorio está vacío")
    void executeShouldThrowWhenRequiredFieldIsBlank() {
        // --- arrange ---
        givenExistingActivity();
        // --- assert ---
        thenEachBlankRequiredFieldThrows();
    }

    // --- arrange ---

    private void givenExistingActivity() {
        Activity existing = Activity.builder()
                .id(ACTIVITY_ID)
                .type(ActivityType.BREATHING)
                .title("Original")
                .build();
        when(activityRepository.findById(ACTIVITY_ID)).thenReturn(Optional.of(existing));
    }

    private void givenActivityNotFound() {
        when(activityRepository.findById(MISSING_ID)).thenReturn(Optional.empty());
    }

    private void givenRepositoryEchoesSavedActivity() {
        when(activityRepository.save(any(Activity.class))).thenAnswer(invocation -> invocation.getArgument(0));
    }

    // --- act ---

    private Activity updateWithValidRequest() {
        UpdateActivityConfigRequest request = new UpdateActivityConfigRequest(
                -0.5, 0.5, -0.5, 0.5, -0.5, 0.5, 0.1, -0.2, 0.3,
                "Nuevo Titulo", "Nueva Desc", "keyword", "/new-path");
        return useCase.execute(ACTIVITY_ID, request);
    }

    // --- assert ---

    private void thenUpdatedActivityMatchesRequest(Activity result) {
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

    private void thenUpdateThrowsResourceNotFound() {
        UpdateActivityConfigRequest request = new UpdateActivityConfigRequest(
                -0.5, 0.5, -0.5, 0.5, -0.5, 0.5, 0.1, 0.1, 0.1, "Titulo", "Desc", "key", "/path");
        assertThatThrownBy(() -> useCase.execute(MISSING_ID, request))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("99");
    }

    private void thenEachOutOfRangeFieldThrows() {
        thenUpdateThrowsBusinessRule(new UpdateActivityConfigRequest(
                -1.5, 0.5, -0.5, 0.5, -0.5, 0.5, 0.1, 0.1, 0.1, "T", "D", "K", "/p"),
                "valenceMin debe estar entre -1.0 y 1.0");
        thenUpdateThrowsBusinessRule(new UpdateActivityConfigRequest(
                -0.5, 1.5, -0.5, 0.5, -0.5, 0.5, 0.1, 0.1, 0.1, "T", "D", "K", "/p"),
                "valenceMax debe estar entre -1.0 y 1.0");
        thenUpdateThrowsBusinessRule(new UpdateActivityConfigRequest(
                -0.5, 0.5, -2.0, 0.5, -0.5, 0.5, 0.1, 0.1, 0.1, "T", "D", "K", "/p"),
                "arousalMin debe estar entre -1.0 y 1.0");
        thenUpdateThrowsBusinessRule(new UpdateActivityConfigRequest(
                -0.5, 0.5, -0.5, 2.5, -0.5, 0.5, 0.1, 0.1, 0.1, "T", "D", "K", "/p"),
                "arousalMax debe estar entre -1.0 y 1.0");
        thenUpdateThrowsBusinessRule(new UpdateActivityConfigRequest(
                -0.5, 0.5, -0.5, 0.5, -1.1, 0.5, 0.1, 0.1, 0.1, "T", "D", "K", "/p"),
                "dominanceMin debe estar entre -1.0 y 1.0");
        thenUpdateThrowsBusinessRule(new UpdateActivityConfigRequest(
                -0.5, 0.5, -0.5, 0.5, -0.5, 1.01, 0.1, 0.1, 0.1, "T", "D", "K", "/p"),
                "dominanceMax debe estar entre -1.0 y 1.0");
        thenUpdateThrowsBusinessRule(new UpdateActivityConfigRequest(
                -0.5, 0.5, -0.5, 0.5, -0.5, 0.5, -1.05, 0.1, 0.1, "T", "D", "K", "/p"),
                "effectValence debe estar entre -1.0 y 1.0");
        thenUpdateThrowsBusinessRule(new UpdateActivityConfigRequest(
                -0.5, 0.5, -0.5, 0.5, -0.5, 0.5, 0.1, 1.05, 0.1, "T", "D", "K", "/p"),
                "effectArousal debe estar entre -1.0 y 1.0");
        thenUpdateThrowsBusinessRule(new UpdateActivityConfigRequest(
                -0.5, 0.5, -0.5, 0.5, -0.5, 0.5, 0.1, 0.1, -1.01, "T", "D", "K", "/p"),
                "effectDominance debe estar entre -1.0 y 1.0");
    }

    private void thenEachMinGreaterThanMaxThrows() {
        thenUpdateThrowsBusinessRule(new UpdateActivityConfigRequest(
                0.6, 0.5, -0.5, 0.5, -0.5, 0.5, 0.1, 0.1, 0.1, "T", "D", "K", "/p"),
                "valenceMin no puede ser mayor que valenceMax");
        thenUpdateThrowsBusinessRule(new UpdateActivityConfigRequest(
                -0.5, 0.5, 0.8, 0.5, -0.5, 0.5, 0.1, 0.1, 0.1, "T", "D", "K", "/p"),
                "arousalMin no puede ser mayor que arousalMax");
        thenUpdateThrowsBusinessRule(new UpdateActivityConfigRequest(
                -0.5, 0.5, -0.5, 0.5, 0.9, 0.5, 0.1, 0.1, 0.1, "T", "D", "K", "/p"),
                "dominanceMin no puede ser mayor que dominanceMax");
    }

    private void thenEachBlankRequiredFieldThrows() {
        thenUpdateThrowsBusinessRule(new UpdateActivityConfigRequest(
                -0.5, 0.5, -0.5, 0.5, -0.5, 0.5, 0.1, 0.1, 0.1, null, "D", "K", "/p"),
                "El título es obligatorio");
        thenUpdateThrowsBusinessRule(new UpdateActivityConfigRequest(
                -0.5, 0.5, -0.5, 0.5, -0.5, 0.5, 0.1, 0.1, 0.1, "  ", "D", "K", "/p"),
                "El título es obligatorio");
        thenUpdateThrowsBusinessRule(new UpdateActivityConfigRequest(
                -0.5, 0.5, -0.5, 0.5, -0.5, 0.5, 0.1, 0.1, 0.1, "T", null, "K", "/p"),
                "La descripción es obligatoria");
        thenUpdateThrowsBusinessRule(new UpdateActivityConfigRequest(
                -0.5, 0.5, -0.5, 0.5, -0.5, 0.5, 0.1, 0.1, 0.1, "T", "", "K", "/p"),
                "La descripción es obligatoria");
        thenUpdateThrowsBusinessRule(new UpdateActivityConfigRequest(
                -0.5, 0.5, -0.5, 0.5, -0.5, 0.5, 0.1, 0.1, 0.1, "T", "D", "K", null),
                "La ruta de navegación es obligatoria");
        thenUpdateThrowsBusinessRule(new UpdateActivityConfigRequest(
                -0.5, 0.5, -0.5, 0.5, -0.5, 0.5, 0.1, 0.1, 0.1, "T", "D", "K", "  "),
                "La ruta de navegación es obligatoria");
    }

    private void thenUpdateThrowsBusinessRule(UpdateActivityConfigRequest request, String expectedMessage) {
        assertThatThrownBy(() -> useCase.execute(ACTIVITY_ID, request))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessage(expectedMessage);
    }
}
