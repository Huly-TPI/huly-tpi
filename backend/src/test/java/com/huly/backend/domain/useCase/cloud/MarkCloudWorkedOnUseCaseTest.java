package com.huly.backend.domain.useCase.cloud;

import com.huly.backend.domain.dto.cloud.MarkCloudWorkedOnRequest;
import com.huly.backend.domain.dto.cloud.MarkCloudWorkedOnResponse;
import com.huly.backend.domain.exception.ResourceNotFoundException;
import com.huly.backend.domain.mapper.cloud.MarkCloudWorkedOnMapper;
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
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MarkCloudWorkedOnUseCaseTest {

    private static final Long USER_ID = 7L;
    private static final Long THOUGHT_ID = 55L;

    @Mock
    private CloudThoughtRepository cloudThoughtRepository;

    private MarkCloudWorkedOnUseCase useCase;

    @BeforeEach
    void setUp() {
        useCase = new MarkCloudWorkedOnUseCase(cloudThoughtRepository, new MarkCloudWorkedOnMapper());
    }

    @Test
    @DisplayName("Marca el pensamiento como trabajado y devuelve su id cuando existe")
    void executeShouldMarkWorkedOnWhenThoughtExists() {
        givenExistingThought();

        MarkCloudWorkedOnResponse result = mark();

        thenMarkedWorkedOn(result);
    }

    @Test
    @DisplayName("Falla y no marca nada cuando el pensamiento no existe")
    void executeShouldThrowAndNotMarkWhenThoughtDoesNotExist() {
        givenMissingThought();

        thenMarkThrowsNotFound();
        thenNothingWasMarked();
    }

    // --- arrange ---

    private void givenExistingThought() {
        when(cloudThoughtRepository.findByIdAndUserId(THOUGHT_ID, USER_ID))
                .thenReturn(Optional.of(thought()));
    }

    private void givenMissingThought() {
        when(cloudThoughtRepository.findByIdAndUserId(THOUGHT_ID, USER_ID))
                .thenReturn(Optional.empty());
    }

    private CloudThought thought() {
        return CloudThought.builder()
                .id(THOUGHT_ID)
                .userId(USER_ID)
                .text("hola")
                .status(CloudStatus.ACTIVE)
                .workedOn(false)
                .createdAt(Instant.parse("2026-02-02T08:00:00Z"))
                .build();
    }

    // --- act ---

    private MarkCloudWorkedOnResponse mark() {
        return useCase.execute(new MarkCloudWorkedOnRequest(THOUGHT_ID, USER_ID));
    }

    // --- assert ---

    private void thenMarkedWorkedOn(MarkCloudWorkedOnResponse result) {
        assertThat(result.id()).isEqualTo(THOUGHT_ID);
        verify(cloudThoughtRepository).markWorkedOn(THOUGHT_ID);
    }

    private void thenMarkThrowsNotFound() {
        assertThatThrownBy(this::mark).isInstanceOf(ResourceNotFoundException.class);
    }

    private void thenNothingWasMarked() {
        verify(cloudThoughtRepository, never()).markWorkedOn(THOUGHT_ID);
    }
}
