package com.huly.backend.infrastructure.repository.jpaRepository.implementation;

import com.huly.backend.domain.model.RiskWord;
import com.huly.backend.domain.model.enums.RiskSeverity;
import com.huly.backend.infrastructure.repository.entity.RiskWordEntity;
import com.huly.backend.infrastructure.repository.jpaRepository.interfaces.IRiskWordJpaRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.domain.Specification;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RiskWordRepositoryImplTest {

    @Mock
    private IRiskWordJpaRepository jpa;

    @InjectMocks
    private RiskWordRepositoryImpl repositoryImpl;

    @Test
    void save_shouldMapDomainToEntityBeforePersisting() {
        RiskWord domain = RiskWord.builder().word("suicidio").severity(RiskSeverity.HIGH).active(true).build();
        RiskWordEntity returned = RiskWordEntity.builder().id(1L).word("suicidio").severity(RiskSeverity.HIGH).active(true).build();
        when(jpa.save(any(RiskWordEntity.class))).thenReturn(returned);

        ArgumentCaptor<RiskWordEntity> captor = ArgumentCaptor.forClass(RiskWordEntity.class);
        repositoryImpl.save(domain);

        verify(jpa).save(captor.capture());
        RiskWordEntity captured = captor.getValue();
        assertThat(captured.getWord()).isEqualTo("suicidio");
        assertThat(captured.getSeverity()).isEqualTo(RiskSeverity.HIGH);
        assertThat(captured.isActive()).isTrue();
    }

    @Test
    void save_shouldMapEntityToDomainAfterPersisting() {
        RiskWord domain = RiskWord.builder().word("suicidio").severity(RiskSeverity.HIGH).active(true).build();
        RiskWordEntity returned = RiskWordEntity.builder().id(42L).word("suicidio").severity(RiskSeverity.HIGH).active(true).build();
        when(jpa.save(any(RiskWordEntity.class))).thenReturn(returned);

        RiskWord result = repositoryImpl.save(domain);

        assertThat(result.getId()).isEqualTo(42L);
        assertThat(result.getWord()).isEqualTo("suicidio");
        assertThat(result.getSeverity()).isEqualTo(RiskSeverity.HIGH);
    }

    @Test
    void findById_shouldReturnMappedDomain_whenEntityExists() {
        RiskWordEntity entity = RiskWordEntity.builder().id(1L).word("ansiedad").severity(RiskSeverity.MEDIUM).active(true).build();
        when(jpa.findById(1L)).thenReturn(Optional.of(entity));

        Optional<RiskWord> result = repositoryImpl.findById(1L);

        assertThat(result).isPresent();
        assertThat(result.get().getWord()).isEqualTo("ansiedad");
    }

    @Test
    void findById_shouldReturnEmpty_whenEntityDoesNotExist() {
        when(jpa.findById(99L)).thenReturn(Optional.empty());

        Optional<RiskWord> result = repositoryImpl.findById(99L);

        assertThat(result).isEmpty();
    }

    @Test
    @SuppressWarnings("unchecked")
    void findAll_shouldReturnMappedPage() {
        RiskWordEntity entity = RiskWordEntity.builder().id(1L).word("suicidio").severity(RiskSeverity.HIGH).active(true).build();
        Page<RiskWordEntity> entityPage = new PageImpl<>(List.of(entity));
        when(jpa.findAll(any(Specification.class), any(PageRequest.class))).thenReturn(entityPage);

        Page<RiskWord> result = repositoryImpl.findAll(null, null, null, PageRequest.of(0, 20));

        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getWord()).isEqualTo("suicidio");
    }
}
