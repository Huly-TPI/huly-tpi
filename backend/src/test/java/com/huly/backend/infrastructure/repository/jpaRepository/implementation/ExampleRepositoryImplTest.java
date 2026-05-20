package com.huly.backend.infrastructure.repository.jpaRepository.implementation;

import com.huly.backend.domain.model.Example;
import com.huly.backend.infrastructure.repository.entity.ExampleEntity;
import com.huly.backend.infrastructure.repository.jpaRepository.interfaces.IExampleJpaRepository;
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
class ExampleRepositoryImplTest {

    @Mock
    private IExampleJpaRepository jpa;

    @InjectMocks
    private ExampleRepositoryImpl repositoryImpl;

    @Test
    void save_shouldMapDomainToEntityBeforePersisting() {
        Example domain = Example.builder().name("Test").description("Desc").build();
        ExampleEntity returnedEntity = ExampleEntity.builder().id(1L).name("Test").description("Desc").active(true).build();
        when(jpa.save(any(ExampleEntity.class))).thenReturn(returnedEntity);

        ArgumentCaptor<ExampleEntity> captor = ArgumentCaptor.forClass(ExampleEntity.class);

        repositoryImpl.save(domain);

        verify(jpa).save(captor.capture());
        ExampleEntity captured = captor.getValue();
        assertThat(captured.getName()).isEqualTo("Test");
        assertThat(captured.getDescription()).isEqualTo("Desc");
        assertThat(captured.isActive()).isTrue();
        assertThat(captured.getId()).isNull();
    }

    @Test
    void save_shouldMapEntityToDomainAfterPersisting() {
        Example domain = Example.builder().name("Test").description("Desc").build();
        ExampleEntity returnedEntity = ExampleEntity.builder().id(42L).name("Test").description("Desc").active(true).build();
        when(jpa.save(any(ExampleEntity.class))).thenReturn(returnedEntity);

        Example result = repositoryImpl.save(domain);

        assertThat(result.getId()).isEqualTo(42L);
        assertThat(result.getName()).isEqualTo("Test");
        assertThat(result.getDescription()).isEqualTo("Desc");
    }

    @Test
    void save_shouldAlwaysSetActiveTrueOnNewEntity() {
        Example domain = Example.builder().name("Any").description(null).build();
        when(jpa.save(any(ExampleEntity.class)))
                .thenReturn(ExampleEntity.builder().id(1L).name("Any").active(true).build());

        ArgumentCaptor<ExampleEntity> captor = ArgumentCaptor.forClass(ExampleEntity.class);

        repositoryImpl.save(domain);

        verify(jpa).save(captor.capture());
        assertThat(captor.getValue().isActive()).isTrue();
    }
}
