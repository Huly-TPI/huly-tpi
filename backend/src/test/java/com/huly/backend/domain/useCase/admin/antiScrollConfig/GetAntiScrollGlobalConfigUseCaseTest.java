package com.huly.backend.domain.useCase.admin.antiScrollConfig;

import com.huly.backend.domain.model.extension.AntiScrollGlobalConfig;
import com.huly.backend.domain.repository.extension.AntiScrollGlobalConfigRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class GetAntiScrollGlobalConfigUseCaseTest {

    private AntiScrollGlobalConfigRepository antiScrollConfigRepository;
    private GetAntiScrollGlobalConfigUseCase useCase;

    @BeforeEach
    void setUp() {
        antiScrollConfigRepository = mock(AntiScrollGlobalConfigRepository.class);
        useCase = new GetAntiScrollGlobalConfigUseCase(antiScrollConfigRepository);
    }

    @Test
    void execute_shouldReturnStoredConfig_whenConfigExists() {
        AntiScrollGlobalConfig config = AntiScrollGlobalConfig.builder()
                .id(1L)
                .defaultPauseIntervalMinutes(25)
                .termsAndConditions("terminos guardados")
                .build();
        when(antiScrollConfigRepository.findFirst()).thenReturn(Optional.of(config));

        GetAntiScrollGlobalConfigResponse response = useCase.execute();

        assertThat(response.defaultPauseIntervalMinutes()).isEqualTo(25);
        assertThat(response.termsAndConditions()).isEqualTo("terminos guardados");
    }

    @Test
    void execute_shouldReturnDefaultConfig_whenConfigDoesNotExist() {
        when(antiScrollConfigRepository.findFirst()).thenReturn(Optional.empty());

        GetAntiScrollGlobalConfigResponse response = useCase.execute();

        assertThat(response.defaultPauseIntervalMinutes()).isEqualTo(20);
        assertThat(response.termsAndConditions())
                .isEqualTo("El modo anti-scroll es simplemente una herramienta para acompa\u00f1arte cuando sientas que necesit\u00e1s frenar un poco. No hay reglas estrictas ni metas que cumplir. Activalo cuando quieras priorizar tu concentraci\u00f3n o desconectar del ruido, y apagalo cuando tengas ganas de explorar libremente. \u00a1Cero presiones, el ritmo lo marc\u00e1s vos!");
    }
}
