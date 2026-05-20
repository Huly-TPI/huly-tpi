package com.huly.backend.domain.useCase;

import com.huly.backend.domain.model.Example;
import com.huly.backend.domain.repository.ExampleRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CreateUseCaseTest {

    @Mock
    private ExampleRepository exampleRepository;

    @InjectMocks
    private CreateUseCase createUseCase;

    @Test
    void execute_shouldBuildExampleWithNullId_andDelegateToRepository() {
        Example savedExample = Example.builder().id(1L).name("Test").description("Desc").build();
        when(exampleRepository.save(any(Example.class))).thenReturn(savedExample);

        ArgumentCaptor<Example> captor = ArgumentCaptor.forClass(Example.class);

        createUseCase.execute("Test", "Desc");

        verify(exampleRepository).save(captor.capture());
        Example captured = captor.getValue();
        assertThat(captured.getId()).isNull();
        assertThat(captured.getName()).isEqualTo("Test");
        assertThat(captured.getDescription()).isEqualTo("Desc");
    }

    @Test
    void execute_shouldReturnWhatRepositoryReturns() {
        Example savedExample = Example.builder().id(1L).name("Test").description("Desc").build();
        when(exampleRepository.save(any(Example.class))).thenReturn(savedExample);

        Example result = createUseCase.execute("Test", "Desc");

        assertThat(result).isSameAs(savedExample);
    }

    @Test
    void execute_shouldCallRepositorySaveExactlyOnce() {
        when(exampleRepository.save(any(Example.class)))
                .thenReturn(Example.builder().id(1L).name("Test").description("Desc").build());

        createUseCase.execute("Test", "Desc");

        verify(exampleRepository).save(any(Example.class));
    }
}
