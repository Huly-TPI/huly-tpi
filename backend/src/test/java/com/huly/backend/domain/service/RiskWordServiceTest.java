package com.huly.backend.domain.service;

import com.huly.backend.domain.model.RiskWord;
import com.huly.backend.domain.model.enums.RiskSeverity;
import com.huly.backend.domain.repository.RiskWordRepository;
import com.huly.backend.exception.BadRequestException;
import com.huly.backend.exception.ConflictException;
import com.huly.backend.exception.NotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RiskWordServiceTest {

    @Mock
    private RiskWordRepository riskWordRepository;

    @InjectMocks
    private RiskWordService riskWordService;

    // ── create ──────────────────────────────────────────────────────────────

    @Test
    void create_shouldSaveAndReturn_whenWordIsNew() {
        RiskWord input = RiskWord.builder().word("suicidio").severity(RiskSeverity.HIGH).active(true).build();
        RiskWord saved  = RiskWord.builder().id(1L).word("suicidio").severity(RiskSeverity.HIGH).active(true).build();
        when(riskWordRepository.existsByWord("suicidio")).thenReturn(false);
        when(riskWordRepository.save(input)).thenReturn(saved);

        RiskWord result = riskWordService.create(input);

        assertThat(result.getId()).isEqualTo(1L);
        verify(riskWordRepository).save(input);
    }

    @Test
    void create_shouldThrowConflict_whenWordAlreadyExists() {
        RiskWord input = RiskWord.builder().word("suicidio").severity(RiskSeverity.HIGH).active(true).build();
        when(riskWordRepository.existsByWord("suicidio")).thenReturn(true);

        assertThatThrownBy(() -> riskWordService.create(input))
                .isInstanceOf(ConflictException.class)
                .hasMessageContaining("suicidio");

        verify(riskWordRepository, never()).save(any());
    }

    // ── update ──────────────────────────────────────────────────────────────

    @Test
    void update_shouldModifyFieldsAndSave_whenIdExistsAndWordIsUnique() {
        RiskWord existing = RiskWord.builder().id(1L).word("ansiedad").severity(RiskSeverity.LOW).active(true).build();
        RiskWord patch    = RiskWord.builder().word("panico").description("desc").severity(RiskSeverity.MEDIUM).build();
        when(riskWordRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(riskWordRepository.existsByWordAndIdNot("panico", 1L)).thenReturn(false);
        when(riskWordRepository.save(existing)).thenReturn(existing);

        riskWordService.update(1L, patch);

        assertThat(existing.getWord()).isEqualTo("panico");
        assertThat(existing.getSeverity()).isEqualTo(RiskSeverity.MEDIUM);
        verify(riskWordRepository).save(existing);
    }

    @Test
    void update_shouldThrowNotFound_whenIdDoesNotExist() {
        when(riskWordRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> riskWordService.update(99L, RiskWord.builder().word("x").build()))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("99");
    }

    @Test
    void update_shouldThrowConflict_whenNewWordBelongsToAnotherRecord() {
        RiskWord existing = RiskWord.builder().id(1L).word("ansiedad").severity(RiskSeverity.LOW).active(true).build();
        when(riskWordRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(riskWordRepository.existsByWordAndIdNot("panico", 1L)).thenReturn(true);

        assertThatThrownBy(() -> riskWordService.update(1L, RiskWord.builder().word("panico").build()))
                .isInstanceOf(ConflictException.class)
                .hasMessageContaining("panico");
    }

    @Test
    void update_shouldSkipDuplicateCheck_whenWordIsUnchanged() {
        RiskWord existing = RiskWord.builder().id(1L).word("ansiedad").severity(RiskSeverity.LOW).active(true).build();
        when(riskWordRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(riskWordRepository.save(any())).thenReturn(existing);

        riskWordService.update(1L, RiskWord.builder().word("ansiedad").severity(RiskSeverity.HIGH).build());

        verify(riskWordRepository, never()).existsByWordAndIdNot(any(), any());
    }

    // ── delete ──────────────────────────────────────────────────────────────

    @Test
    void delete_shouldCallDeleteById_whenIdExists() {
        when(riskWordRepository.existsById(1L)).thenReturn(true);

        riskWordService.delete(1L);

        verify(riskWordRepository).deleteById(1L);
    }

    @Test
    void delete_shouldThrowNotFound_whenIdDoesNotExist() {
        when(riskWordRepository.existsById(99L)).thenReturn(false);

        assertThatThrownBy(() -> riskWordService.delete(99L))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("99");

        verify(riskWordRepository, never()).deleteById(any());
    }

    // ── list ────────────────────────────────────────────────────────────────

    @Test
    void list_shouldReturnPage_whenFiltersAreValid() {
        Page<RiskWord> page = new PageImpl<>(List.of());
        when(riskWordRepository.findAll(null, null, null, PageRequest.of(0, 20))).thenReturn(page);

        Page<RiskWord> result = riskWordService.list(null, null, null, PageRequest.of(0, 20));

        assertThat(result).isNotNull();
    }

    @Test
    void list_shouldThrowBadRequest_whenSeverityIsInvalid() {
        assertThatThrownBy(() -> riskWordService.list(null, null, "INVALIDA", PageRequest.of(0, 20)))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("LOW, MEDIUM, HIGH");
    }
}
