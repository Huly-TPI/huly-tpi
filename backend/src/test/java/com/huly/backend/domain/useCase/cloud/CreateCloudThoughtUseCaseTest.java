package com.huly.backend.domain.useCase.cloud;

import com.huly.backend.domain.dto.cloud.CreateCloudThoughtRequest;
import com.huly.backend.domain.dto.cloud.CreateCloudThoughtResponse;
import com.huly.backend.domain.mapper.cloud.CreateCloudThoughtMapper;
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CreateCloudThoughtUseCaseTest {

    private static final Long USER_ID = 7L;
    private static final Long THOUGHT_ID = 55L;
    private static final String TEXT = "hola";
    private static final Instant CREATED_AT = Instant.parse("2026-02-02T08:00:00Z");

    @Mock
    private CloudThoughtRepository cloudThoughtRepository;

    private CreateCloudThoughtUseCase useCase;

    @BeforeEach
    void setUp() {
        useCase = new CreateCloudThoughtUseCase(cloudThoughtRepository, new CreateCloudThoughtMapper());
    }

    @Test
    @DisplayName("Guarda el pensamiento delegando el userId y el texto en el repositorio")
    void executeShouldPersistUsingUserIdAndText() {
        givenSaved(persistedThought());

        create();

        thenSavedWithUserIdAndText();
    }

    @Test
    @DisplayName("Mapea el pensamiento guardado a la respuesta de dominio")
    void executeShouldMapPersistedThoughtToResponse() {
        givenSaved(persistedThought());

        CreateCloudThoughtResponse result = create();

        thenResponseMatchesPersisted(result);
    }

    // --- arrange ---

    private void givenSaved(CloudThought thought) {
        when(cloudThoughtRepository.save(USER_ID, TEXT)).thenReturn(thought);
    }

    private CloudThought persistedThought() {
        return CloudThought.builder()
                .id(THOUGHT_ID)
                .userId(USER_ID)
                .text(TEXT)
                .status(CloudStatus.ACTIVE)
                .workedOn(false)
                .createdAt(CREATED_AT)
                .build();
    }

    // --- act ---

    private CreateCloudThoughtResponse create() {
        return useCase.execute(new CreateCloudThoughtRequest(USER_ID, TEXT));
    }

    // --- assert ---

    private void thenSavedWithUserIdAndText() {
        verify(cloudThoughtRepository).save(USER_ID, TEXT);
    }

    private void thenResponseMatchesPersisted(CreateCloudThoughtResponse result) {
        assertThat(result.id()).isEqualTo(THOUGHT_ID);
        assertThat(result.text()).isEqualTo(TEXT);
        assertThat(result.workedOn()).isFalse();
        assertThat(result.createdAt()).isEqualTo(CREATED_AT);
    }
}
