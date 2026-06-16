package com.huly.backend.infrastructure.config.useCase;

import com.huly.backend.domain.repository.LanternThoughtRepository;
import com.huly.backend.domain.useCase.lantern.CreateLanternThoughtUseCase;
import com.huly.backend.domain.useCase.lantern.ListLanternThoughtsUseCase;
import com.huly.backend.domain.useCase.lantern.MarkWorkedOnUseCase;
import com.huly.backend.domain.useCase.lantern.UpdateLanternStatusUseCase;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

class LanternUseCaseConfigTest {

    private final LanternUseCaseConfig config = new LanternUseCaseConfig();
    private final LanternThoughtRepository repository = mock(LanternThoughtRepository.class);

    @Test
    void createLanternThoughtUseCase_shouldReturnNonNullBean() {
        CreateLanternThoughtUseCase useCase = config.createLanternThoughtUseCase(repository);
        assertThat(useCase).isNotNull();
    }

    @Test
    void listLanternThoughtsUseCase_shouldReturnNonNullBean() {
        ListLanternThoughtsUseCase useCase = config.listLanternThoughtsUseCase(repository);
        assertThat(useCase).isNotNull();
    }

    @Test
    void updateLanternStatusUseCase_shouldReturnNonNullBean() {
        UpdateLanternStatusUseCase useCase = config.updateLanternStatusUseCase(repository);
        assertThat(useCase).isNotNull();
    }

    @Test
    void markWorkedOnUseCase_shouldReturnNonNullBean() {
        MarkWorkedOnUseCase useCase = config.markWorkedOnUseCase(repository);
        assertThat(useCase).isNotNull();
    }
}
