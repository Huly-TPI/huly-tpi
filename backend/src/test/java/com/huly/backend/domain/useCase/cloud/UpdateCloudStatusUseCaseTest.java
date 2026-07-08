package com.huly.backend.domain.useCase.cloud;

import com.huly.backend.domain.dto.cloud.UpdateCloudStatusRequest;
import com.huly.backend.domain.dto.cloud.UpdateCloudStatusResponse;
import com.huly.backend.domain.exception.ResourceNotFoundException;
import com.huly.backend.domain.mapper.cloud.UpdateCloudStatusMapper;
import com.huly.backend.domain.model.CloudThought;
import com.huly.backend.domain.model.enums.CloudStatus;
import com.huly.backend.domain.repository.CloudThoughtRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UpdateCloudStatusUseCaseTest {

    private static final Long USER_ID = 7L;
    private static final Long THOUGHT_ID = 55L;
    private static final Instant CREATED_AT = Instant.parse("2026-02-02T08:00:00Z");

    @Mock
    private CloudThoughtRepository cloudThoughtRepository;

    private UpdateCloudStatusUseCase useCase;

    @BeforeEach
    void setUp() {
        useCase = new UpdateCloudStatusUseCase(cloudThoughtRepository, new UpdateCloudStatusMapper());
    }

    @Test
    @DisplayName("Falla cuando el pensamiento no existe")
    void executeShouldThrowWhenThoughtDoesNotExist() {
        givenMissingThought();

        thenUpdateThrowsNotFound(CloudStatus.COMPLETED);
    }

    @Test
    @DisplayName("Falla cuando el pensamiento no está activo")
    void executeShouldThrowWhenThoughtIsNotActive() {
        givenThoughtWithStatus(CloudStatus.COMPLETED);

        thenUpdateThrowsIllegalState(CloudStatus.COMPLETED);
    }

    @Test
    @DisplayName("Falla cuando la transición de estado no está permitida")
    void executeShouldThrowWhenTransitionIsNotAllowed() {
        givenThoughtWithStatus(CloudStatus.ACTIVE);

        thenUpdateThrowsInvalidTransition(CloudStatus.ACTIVE);
    }

    @Test
    @DisplayName("Completa el pensamiento activo cuando la transición es a COMPLETED")
    void executeShouldCompleteActiveThought() {
        givenThoughtWithStatus(CloudStatus.ACTIVE);
        givenUpdatedTo(CloudStatus.COMPLETED);

        UpdateCloudStatusResponse result = update(CloudStatus.COMPLETED);

        thenStatusUpdatedTo(result, CloudStatus.COMPLETED);
    }

    @Test
    @DisplayName("Cancela el pensamiento activo cuando la transición es a CANCELLED")
    void executeShouldCancelActiveThought() {
        givenThoughtWithStatus(CloudStatus.ACTIVE);
        givenUpdatedTo(CloudStatus.CANCELLED);

        UpdateCloudStatusResponse result = update(CloudStatus.CANCELLED);

        thenStatusUpdatedTo(result, CloudStatus.CANCELLED);
    }

    // --- arrange ---

    private void givenMissingThought() {
        when(cloudThoughtRepository.findByIdAndUserId(THOUGHT_ID, USER_ID))
                .thenReturn(Optional.empty());
    }

    private void givenThoughtWithStatus(CloudStatus status) {
        when(cloudThoughtRepository.findByIdAndUserId(THOUGHT_ID, USER_ID))
                .thenReturn(Optional.of(thought(status)));
    }

    private void givenUpdatedTo(CloudStatus status) {
        when(cloudThoughtRepository.updateStatus(THOUGHT_ID, status)).thenReturn(thought(status));
    }

    private CloudThought thought(CloudStatus status) {
        return CloudThought.builder()
                .id(THOUGHT_ID)
                .userId(USER_ID)
                .text("hola")
                .status(status)
                .workedOn(false)
                .createdAt(CREATED_AT)
                .build();
    }

    // --- act ---

    private UpdateCloudStatusResponse update(CloudStatus newStatus) {
        return useCase.execute(new UpdateCloudStatusRequest(THOUGHT_ID, USER_ID, newStatus));
    }

    // --- assert ---

    private void thenStatusUpdatedTo(UpdateCloudStatusResponse result, CloudStatus status) {
        assertThat(result.id()).isEqualTo(THOUGHT_ID);
        assertThat(result.status()).isEqualTo(status);
        assertThat(result.text()).isEqualTo("hola");
        assertThat(result.createdAt()).isEqualTo(CREATED_AT);
    }

    private void thenUpdateThrowsNotFound(CloudStatus newStatus) {
        assertThatThrownBy(() -> update(newStatus)).isInstanceOf(ResourceNotFoundException.class);
    }

    private void thenUpdateThrowsIllegalState(CloudStatus newStatus) {
        assertThatThrownBy(() -> update(newStatus)).isInstanceOf(IllegalStateException.class);
    }

    private void thenUpdateThrowsInvalidTransition(CloudStatus newStatus) {
        assertThatThrownBy(() -> update(newStatus))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Transición de estado no permitida");
    }
}
