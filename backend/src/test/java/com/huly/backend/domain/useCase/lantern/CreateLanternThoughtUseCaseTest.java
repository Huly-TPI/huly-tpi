package com.huly.backend.domain.useCase.lantern;

import com.huly.backend.domain.model.LanternThought;
import com.huly.backend.domain.model.enums.LanternStatus;
import com.huly.backend.domain.repository.LanternThoughtRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CreateLanternThoughtUseCaseTest {

    @Mock
    private LanternThoughtRepository lanternThoughtRepository;

    @InjectMocks
    private CreateLanternThoughtUseCase useCase;

    @Test
    void execute_shouldDelegateToRepositoryAndReturnResult() {
        LanternThought expected = LanternThought.builder()
                .id(1L).userId(10L).text("me siento ansioso")
                .status(LanternStatus.ACTIVE).workedOn(false).createdAt(Instant.now()).build();
        when(lanternThoughtRepository.save(10L, "me siento ansioso")).thenReturn(expected);

        LanternThought result = useCase.execute(10L, "me siento ansioso");

        assertThat(result).isEqualTo(expected);
        verify(lanternThoughtRepository).save(10L, "me siento ansioso");
    }

    @Test
    void execute_shouldPassUserIdAndTextToRepository() {
        LanternThought expected = LanternThought.builder().id(2L).userId(5L).text("otro pensamiento").build();
        when(lanternThoughtRepository.save(5L, "otro pensamiento")).thenReturn(expected);

        useCase.execute(5L, "otro pensamiento");

        verify(lanternThoughtRepository).save(5L, "otro pensamiento");
    }
}
