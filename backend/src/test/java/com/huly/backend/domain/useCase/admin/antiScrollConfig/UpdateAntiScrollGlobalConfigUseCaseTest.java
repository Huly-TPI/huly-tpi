package com.huly.backend.domain.useCase.admin.antiScrollConfig;

import com.huly.backend.domain.model.extension.AntiScrollGlobalConfig;
import com.huly.backend.domain.repository.extension.AntiScrollGlobalConfigRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class UpdateAntiScrollGlobalConfigUseCaseTest {

    private AntiScrollGlobalConfigRepository antiScrollConfigRepository;
    private UpdateAntiScrollGlobalConfigUseCase useCase;

    @BeforeEach
    void setUp() {
        antiScrollConfigRepository = mock(AntiScrollGlobalConfigRepository.class);
        useCase = new UpdateAntiScrollGlobalConfigUseCase(antiScrollConfigRepository);
    }

    @Test
    void execute_shouldCreateConfig_whenNoConfigExists() {
        when(antiScrollConfigRepository.findFirst()).thenReturn(Optional.empty());

        useCase.execute(new UpdateAntiScrollGlobalConfigRequest(15, "nuevos terminos"));

        verify(antiScrollConfigRepository).save(argThat(config ->
                config.getId() == null
                        && config.getDefaultPauseIntervalMinutes() == 15
                        && "nuevos terminos".equals(config.getTermsAndConditions())
        ));
    }

    @Test
    void execute_shouldPreserveId_whenConfigAlreadyExists() {
        AntiScrollGlobalConfig existing = AntiScrollGlobalConfig.builder()
                .id(7L)
                .defaultPauseIntervalMinutes(25)
                .termsAndConditions("anteriores")
                .build();
        when(antiScrollConfigRepository.findFirst()).thenReturn(Optional.of(existing));

        useCase.execute(new UpdateAntiScrollGlobalConfigRequest(30, "actualizados"));

        verify(antiScrollConfigRepository).save(argThat(config ->
                Long.valueOf(7L).equals(config.getId())
                        && config.getDefaultPauseIntervalMinutes() == 30
                        && "actualizados".equals(config.getTermsAndConditions())
        ));
    }
}
