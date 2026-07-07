package com.huly.backend.infrastructure.presentation.mapper.pending;

import com.huly.backend.domain.dto.pending.PendingSubtaskResponse;
import com.huly.backend.domain.model.enums.EstimatedDuration;
import com.huly.backend.domain.model.enums.PendingCategory;
import com.huly.backend.domain.model.enums.PendingStatus;
import com.huly.backend.domain.model.enums.RecommendationResponseDecision;
import com.huly.backend.infrastructure.presentation.dto.pending.AddSubtaskRequest;
import com.huly.backend.infrastructure.presentation.dto.pending.CreatePendingTaskRequest;
import com.huly.backend.infrastructure.presentation.dto.pending.PendingTaskResponse;
import com.huly.backend.infrastructure.presentation.dto.pending.UpdatePendingTaskRequest;
import com.huly.backend.infrastructure.presentation.dto.pending.UpdatePositionRequest;
import com.huly.backend.infrastructure.presentation.dto.pending.RespondToRecommendationRequest;
import com.huly.backend.infrastructure.presentation.exception.BadRequestException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PendingPresentationMapperTest {

    private PendingPresentationMapper mapper;

    @BeforeEach
    void setUp() {
        mapper = new PendingPresentationMapper();
    }

    @Test
    @DisplayName("Mapea DTO de creación HTTP a DTO de caso de uso de dominio")
    void toCreateRequestShouldMapCorrectly() {
        CreatePendingTaskRequest dto = buildCreateDto("Título", "FIFTEEN_MIN", "SALUD");

        var request = mapToCreateRequest(10L, dto);

        thenCreateRequestMatches(request, 10L, "Título", EstimatedDuration.FIFTEEN_MIN, PendingCategory.SALUD);
    }

    @Test
    @DisplayName("Mapea DTO de actualización HTTP a DTO de caso de uso de dominio")
    void toUpdateRequestShouldMapCorrectly() {
        UpdatePendingTaskRequest dto = buildUpdateDto("Título", "ONE_HOUR", "TRABAJO");

        var request = mapToUpdateRequest(1L, 10L, dto);

        thenUpdateRequestMatches(request, 1L, 10L, "Título", EstimatedDuration.ONE_HOUR, PendingCategory.TRABAJO);
    }

    @Test
    @DisplayName("Mapea parámetros HTTP de listado a DTO de caso de uso")
    void toListRequestShouldMapCorrectly() {
        var request = mapToListRequest(10L, "PENDING");
        thenListRequestMatches(request, 10L, PendingStatus.PENDING);

        var requestNull = mapToListRequest(10L, null);
        thenListRequestFilterIsNull(requestNull, 10L);
    }

    @Test
    @DisplayName("Lanza excepción si el estado de filtrado en listado es inválido")
    void toListRequestShouldThrowWhenStatusInvalid() {
        assertThatThrownBy(() -> mapToListRequest(10L, "INVALID"))
                .isInstanceOf(BadRequestException.class);
    }

    @Test
    @DisplayName("Mapea parámetros HTTP de consulta a DTO de caso de uso")
    void toGetRequestShouldMapCorrectly() {
        var request = mapToGetRequest(1L, 10L);
        thenGetRequestMatches(request, 1L, 10L);
    }

    @Test
    @DisplayName("Mapea parámetros HTTP de eliminación a DTO de caso de uso")
    void toDeleteRequestShouldMapCorrectly() {
        var request = mapToDeleteRequest(1L, 10L);
        thenDeleteRequestMatches(request, 1L, 10L);
    }

    @Test
    @DisplayName("Mapea parámetros HTTP de completado a DTO de caso de uso")
    void toCompleteRequestShouldMapCorrectly() {
        var request = mapToCompleteRequest(1L, 10L);
        thenCompleteRequestMatches(request, 1L, 10L);
    }

    @Test
    @DisplayName("Mapea DTO HTTP de adición de subtarea a DTO de caso de uso")
    void toAddSubtaskRequestShouldMapCorrectly() {
        AddSubtaskRequest dto = buildAddSubtaskDto("Subtarea");

        var request = mapToAddSubtaskRequest(1L, 10L, dto);

        thenAddSubtaskRequestMatches(request, 1L, 10L, "Subtarea");
    }

    @Test
    @DisplayName("Mapea parámetros HTTP de alternar subtarea a DTO de caso de uso")
    void toToggleSubtaskRequestShouldMapCorrectly() {
        var request = mapToToggleSubtaskRequest(1L, 100L, 10L);
        thenToggleSubtaskRequestMatches(request, 1L, 100L, 10L);
    }

    @Test
    @DisplayName("Mapea parámetros HTTP de eliminar subtarea a DTO de caso de uso")
    void toDeleteSubtaskRequestShouldMapCorrectly() {
        var request = mapToDeleteSubtaskRequest(1L, 100L, 10L);
        thenDeleteSubtaskRequestMatches(request, 1L, 100L, 10L);
    }

    @Test
    @DisplayName("Mapea DTO HTTP de actualización de posición a DTO de caso de uso")
    void toPositionRequestShouldMapCorrectly() {
        UpdatePositionRequest dto = buildPositionDto(15.0, 20.0);

        var request = mapToPositionRequest(1L, 10L, dto);

        thenPositionRequestMatches(request, 1L, 10L, 15.0, 20.0);
    }

    @Test
    @DisplayName("Mapea DTO HTTP de respuesta a recomendación a DTO de caso de uso")
    void toRespondRequestShouldMapCorrectly() {
        RespondToRecommendationRequest dto = buildRespondDto("ACCEPTED");

        var request = mapToRespondRequest(1L, 10L, dto);

        thenRespondRequestMatches(request, 1L, 10L, RecommendationResponseDecision.ACCEPTED);
    }

    @Test
    @DisplayName("Lanza excepción si la decisión HTTP a la recomendación es inválida o PENDING")
    void toRespondRequestShouldThrowWhenDecisionInvalidOrPending() {
        RespondToRecommendationRequest dtoPending = buildRespondDto("PENDING");
        assertThatThrownBy(() -> mapToRespondRequest(1L, 10L, dtoPending))
                .isInstanceOf(BadRequestException.class);

        RespondToRecommendationRequest dtoInvalid = buildRespondDto("INVALID");
        assertThatThrownBy(() -> mapToRespondRequest(1L, 10L, dtoInvalid))
                .isInstanceOf(BadRequestException.class);
    }

    @Test
    @DisplayName("Mapea DTO de respuesta de dominio a DTO HTTP para el controlador")
    void toTaskResponseShouldMapCorrectly() {
        com.huly.backend.domain.dto.pending.PendingTaskResponse response = buildDomainTaskResponse();

        PendingTaskResponse dto = mapToTaskResponse(response);

        thenTaskResponseMatches(dto, 1L, "Título", "FIFTEEN_MIN", "SALUD", "PENDING", 1);
    }

    @Test
    @DisplayName("Lanza excepción al parsear duración si el string es inválido")
    void parseDurationShouldThrowWhenInvalid() {
        CreatePendingTaskRequest dto = buildCreateDto("Título", "INVALID", "SALUD");
        assertThatThrownBy(() -> mapToCreateRequest(10L, dto))
                .isInstanceOf(BadRequestException.class);
    }

    @Test
    @DisplayName("Lanza excepción al parsear categoría si el string es inválido")
    void parseCategoryShouldThrowWhenInvalid() {
        CreatePendingTaskRequest dto = buildCreateDto("Título", "FIFTEEN_MIN", "INVALID");
        assertThatThrownBy(() -> mapToCreateRequest(10L, dto))
                .isInstanceOf(BadRequestException.class);
    }

    // --- act ---

    private com.huly.backend.domain.dto.pending.CreatePendingTaskRequest mapToCreateRequest(Long userId, CreatePendingTaskRequest dto) {
        return mapper.toCreateRequest(userId, dto);
    }

    private com.huly.backend.domain.dto.pending.UpdatePendingTaskRequest mapToUpdateRequest(Long id, Long userId, UpdatePendingTaskRequest dto) {
        return mapper.toUpdateRequest(id, userId, dto);
    }

    private com.huly.backend.domain.dto.pending.ListPendingTasksRequest mapToListRequest(Long userId, String statusFilter) {
        return mapper.toListRequest(userId, statusFilter);
    }

    private com.huly.backend.domain.dto.pending.GetPendingTaskRequest mapToGetRequest(Long id, Long userId) {
        return mapper.toGetRequest(id, userId);
    }

    private com.huly.backend.domain.dto.pending.DeletePendingTaskRequest mapToDeleteRequest(Long id, Long userId) {
        return mapper.toDeleteRequest(id, userId);
    }

    private com.huly.backend.domain.dto.pending.CompletePendingTaskRequest mapToCompleteRequest(Long id, Long userId) {
        return mapper.toCompleteRequest(id, userId);
    }

    private com.huly.backend.domain.dto.pending.AddPendingSubtaskRequest mapToAddSubtaskRequest(Long taskId, Long userId, AddSubtaskRequest dto) {
        return mapper.toAddSubtaskRequest(taskId, userId, dto);
    }

    private com.huly.backend.domain.dto.pending.TogglePendingSubtaskRequest mapToToggleSubtaskRequest(Long taskId, Long subtaskId, Long userId) {
        return mapper.toToggleSubtaskRequest(taskId, subtaskId, userId);
    }

    private com.huly.backend.domain.dto.pending.DeletePendingSubtaskRequest mapToDeleteSubtaskRequest(Long taskId, Long subtaskId, Long userId) {
        return mapper.toDeleteSubtaskRequest(taskId, subtaskId, userId);
    }

    private com.huly.backend.domain.dto.pending.UpdatePendingPositionRequest mapToPositionRequest(Long id, Long userId, UpdatePositionRequest dto) {
        return mapper.toPositionRequest(id, userId, dto);
    }

    private com.huly.backend.domain.dto.pendingRecommendation.RespondToRecommendationRequest mapToRespondRequest(Long recommendationId, Long userId, RespondToRecommendationRequest dto) {
        return mapper.toRespondRequest(recommendationId, userId, dto);
    }

    private PendingTaskResponse mapToTaskResponse(com.huly.backend.domain.dto.pending.PendingTaskResponse response) {
        return mapper.toTaskResponse(response);
    }

    // --- assert ---

    private void thenCreateRequestMatches(com.huly.backend.domain.dto.pending.CreatePendingTaskRequest request, Long userId, String title, EstimatedDuration duration, PendingCategory category) {
        assertThat(request.userId()).isEqualTo(userId);
        assertThat(request.title()).isEqualTo(title);
        assertThat(request.estimatedDuration()).isEqualTo(duration);
        assertThat(request.category()).isEqualTo(category);
    }

    private void thenUpdateRequestMatches(com.huly.backend.domain.dto.pending.UpdatePendingTaskRequest request, Long id, Long userId, String title, EstimatedDuration duration, PendingCategory category) {
        assertThat(request.id()).isEqualTo(id);
        assertThat(request.userId()).isEqualTo(userId);
        assertThat(request.title()).isEqualTo(title);
        assertThat(request.estimatedDuration()).isEqualTo(duration);
        assertThat(request.category()).isEqualTo(category);
    }

    private void thenListRequestMatches(com.huly.backend.domain.dto.pending.ListPendingTasksRequest request, Long userId, PendingStatus status) {
        assertThat(request.userId()).isEqualTo(userId);
        assertThat(request.statusFilter()).isEqualTo(status);
    }

    private void thenListRequestFilterIsNull(com.huly.backend.domain.dto.pending.ListPendingTasksRequest request, Long userId) {
        assertThat(request.userId()).isEqualTo(userId);
        assertThat(request.statusFilter()).isNull();
    }

    private void thenGetRequestMatches(com.huly.backend.domain.dto.pending.GetPendingTaskRequest request, Long id, Long userId) {
        assertThat(request.id()).isEqualTo(id);
        assertThat(request.userId()).isEqualTo(userId);
    }

    private void thenDeleteRequestMatches(com.huly.backend.domain.dto.pending.DeletePendingTaskRequest request, Long id, Long userId) {
        assertThat(request.id()).isEqualTo(id);
        assertThat(request.userId()).isEqualTo(userId);
    }

    private void thenCompleteRequestMatches(com.huly.backend.domain.dto.pending.CompletePendingTaskRequest request, Long id, Long userId) {
        assertThat(request.id()).isEqualTo(id);
        assertThat(request.userId()).isEqualTo(userId);
    }

    private void thenAddSubtaskRequestMatches(com.huly.backend.domain.dto.pending.AddPendingSubtaskRequest request, Long taskId, Long userId, String text) {
        assertThat(request.taskId()).isEqualTo(taskId);
        assertThat(request.userId()).isEqualTo(userId);
        assertThat(request.text()).isEqualTo(text);
    }

    private void thenToggleSubtaskRequestMatches(com.huly.backend.domain.dto.pending.TogglePendingSubtaskRequest request, Long taskId, Long subtaskId, Long userId) {
        assertThat(request.taskId()).isEqualTo(taskId);
        assertThat(request.subtaskId()).isEqualTo(subtaskId);
        assertThat(request.userId()).isEqualTo(userId);
    }

    private void thenDeleteSubtaskRequestMatches(com.huly.backend.domain.dto.pending.DeletePendingSubtaskRequest request, Long taskId, Long subtaskId, Long userId) {
        assertThat(request.taskId()).isEqualTo(taskId);
        assertThat(request.subtaskId()).isEqualTo(subtaskId);
        assertThat(request.userId()).isEqualTo(userId);
    }

    private void thenPositionRequestMatches(com.huly.backend.domain.dto.pending.UpdatePendingPositionRequest request, Long id, Long userId, double x, double y) {
        assertThat(request.id()).isEqualTo(id);
        assertThat(request.userId()).isEqualTo(userId);
        assertThat(request.positionX()).isEqualTo(x);
        assertThat(request.positionY()).isEqualTo(y);
    }

    private void thenRespondRequestMatches(com.huly.backend.domain.dto.pendingRecommendation.RespondToRecommendationRequest request, Long id, Long userId, RecommendationResponseDecision decision) {
        assertThat(request.recommendationId()).isEqualTo(id);
        assertThat(request.userId()).isEqualTo(userId);
        assertThat(request.decision()).isEqualTo(decision);
    }

    private void thenTaskResponseMatches(PendingTaskResponse dto, Long id, String title, String duration, String category, String status, int subtasksCount) {
        assertThat(dto.id()).isEqualTo(id);
        assertThat(dto.title()).isEqualTo(title);
        assertThat(dto.estimatedDuration()).isEqualTo(duration);
        assertThat(dto.category()).isEqualTo(category);
        assertThat(dto.status()).isEqualTo(status);
        assertThat(dto.subtasks()).hasSize(subtasksCount);
        assertThat(dto.recommended()).isTrue();
    }

    // --- helpers ---

    private CreatePendingTaskRequest buildCreateDto(String title, String duration, String category) {
        return new CreatePendingTaskRequest(title, "Desc", LocalDate.now(), duration, category, List.of());
    }

    private UpdatePendingTaskRequest buildUpdateDto(String title, String duration, String category) {
        return new UpdatePendingTaskRequest(title, "Desc", LocalDate.now(), duration, category);
    }

    private AddSubtaskRequest buildAddSubtaskDto(String text) {
        return new AddSubtaskRequest(text);
    }

    private UpdatePositionRequest buildPositionDto(double x, double y) {
        return new UpdatePositionRequest(x, y);
    }

    private RespondToRecommendationRequest buildRespondDto(String decision) {
        return new RespondToRecommendationRequest(decision);
    }

    private com.huly.backend.domain.dto.pending.PendingTaskResponse buildDomainTaskResponse() {
        return new com.huly.backend.domain.dto.pending.PendingTaskResponse(
                1L, "Título", "Desc", LocalDate.now(), EstimatedDuration.FIFTEEN_MIN, PendingCategory.SALUD,
                PendingStatus.PENDING, List.of(new PendingSubtaskResponse(100L, 1L, "Sub", true, 1)),
                10.0, 20.0, 4.0, Instant.now(), true, Instant.now(), null
        );
    }
}
