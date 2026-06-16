package com.huly.backend.domain.useCase.lantern;

import com.huly.backend.domain.model.LanternThought;
import com.huly.backend.domain.model.enums.LanternStatus;
import com.huly.backend.domain.repository.LanternThoughtRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ListLanternThoughtsUseCaseTest {

    @Mock
    private LanternThoughtRepository lanternThoughtRepository;

    @InjectMocks
    private ListLanternThoughtsUseCase useCase;

    @Test
    void execute_shouldDelegateToRepositoryWithUserId() {
        List<LanternThought> expected = List.of(
                LanternThought.builder().id(1L).userId(5L).text("pensamiento 1").status(LanternStatus.ACTIVE).build(),
                LanternThought.builder().id(2L).userId(5L).text("pensamiento 2").status(LanternStatus.ACTIVE).build()
        );
        when(lanternThoughtRepository.findAllByUserId(5L)).thenReturn(expected);

        List<LanternThought> result = useCase.execute(5L);

        assertThat(result).isEqualTo(expected);
        verify(lanternThoughtRepository).findAllByUserId(5L);
    }

    @Test
    void execute_shouldReturnEmptyList_whenNoThoughtsFound() {
        when(lanternThoughtRepository.findAllByUserId(99L)).thenReturn(List.of());

        List<LanternThought> result = useCase.execute(99L);

        assertThat(result).isEmpty();
    }
}
