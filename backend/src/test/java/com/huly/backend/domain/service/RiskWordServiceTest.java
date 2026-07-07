package com.huly.backend.domain.service;

import com.huly.backend.domain.model.enums.RiskSeverity;
import com.huly.backend.domain.model.riskWord.RiskWord;
import com.huly.backend.domain.repository.chatBotConfig.RiskWordRepository;
import com.huly.backend.domain.service.chat.RiskWordService;
import com.huly.backend.infrastructure.presentation.exception.BadRequestException;
import com.huly.backend.infrastructure.presentation.exception.ConflictException;
import com.huly.backend.infrastructure.presentation.exception.NotFoundException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RiskWordServiceTest {

    @Mock
    private RiskWordRepository riskWordRepository;

    @InjectMocks
    private RiskWordService riskWordService;

    @Test
    @DisplayName("Guarda y devuelve la palabra cuando es nueva")
    void createShouldSaveAndReturnWhenWordIsNew() {
        RiskWord input = word("suicidio", RiskSeverity.HIGH);
        RiskWord saved = savedWord(1L, "suicidio", RiskSeverity.HIGH);
        givenWordDoesNotExist("suicidio");
        givenSaved(input, saved);

        RiskWord result = create(input);

        thenResultId(result, 1L);
        thenSaved(input);
    }

    @Test
    @DisplayName("Normaliza la palabra a minúsculas al crear")
    void createShouldNormalizeWordToLowercase() {
        RiskWord input = word("  Suicidio  ", RiskSeverity.HIGH);
        givenWordDoesNotExist("suicidio");
        givenSavedAny(savedWord(1L, "suicidio", RiskSeverity.HIGH));

        create(input);

        thenWordIs(input, "suicidio");
        thenUniquenessCheckedFor("suicidio");
    }

    @Test
    @DisplayName("Lanza conflicto cuando la palabra ya existe al crear")
    void createShouldThrowConflictWhenWordAlreadyExists() {
        RiskWord input = word("suicidio", RiskSeverity.HIGH);
        givenWordAlreadyExists("suicidio");

        thenCreateThrowsConflict(input, "suicidio");
        thenSaveNeverInvoked();
    }

    @Test
    @DisplayName("Lanza conflicto cuando la palabra ya existe con distinta capitalización al crear")
    void createShouldThrowConflictWhenWordExistsWithDifferentCase() {
        RiskWord input = word("SUICIDIO", RiskSeverity.HIGH);
        givenWordAlreadyExists("suicidio");

        thenCreateThrowsConflict(input, "suicidio");
        thenSaveNeverInvoked();
    }

    @Test
    @DisplayName("Modifica los campos y guarda cuando el id existe y la palabra es única")
    void updateShouldModifyFieldsAndSaveWhenIdExistsAndWordIsUnique() {
        RiskWord existing = existingWord(1L, "ansiedad", RiskSeverity.LOW);
        RiskWord patch = patch("panico", "desc", RiskSeverity.MEDIUM);
        givenFound(1L, existing);
        givenWordNotTakenByAnother("panico", 1L);
        givenSaved(existing, existing);

        update(1L, patch);

        thenWordIs(existing, "panico");
        thenSeverityIs(existing, RiskSeverity.MEDIUM);
        thenSaved(existing);
    }

    @Test
    @DisplayName("Normaliza la palabra a minúsculas al actualizar")
    void updateShouldNormalizeWordToLowercase() {
        RiskWord existing = existingWord(1L, "ansiedad", RiskSeverity.LOW);
        givenFound(1L, existing);
        givenWordNotTakenByAnother("panico", 1L);
        givenSavedAny(existing);

        update(1L, word("  Panico  ", RiskSeverity.HIGH));

        thenWordIs(existing, "panico");
    }

    @Test
    @DisplayName("Lanza no encontrado cuando el id no existe al actualizar")
    void updateShouldThrowNotFoundWhenIdDoesNotExist() {
        givenNotFound(99L);

        thenUpdateThrowsNotFound(99L, wordOnly("x"), "99");
    }

    @Test
    @DisplayName("Lanza conflicto cuando la nueva palabra pertenece a otro registro")
    void updateShouldThrowConflictWhenNewWordBelongsToAnotherRecord() {
        RiskWord existing = existingWord(1L, "ansiedad", RiskSeverity.LOW);
        givenFound(1L, existing);
        givenWordTakenByAnother("panico", 1L);

        thenUpdateThrowsConflict(1L, wordOnly("panico"), "panico");
    }

    @Test
    @DisplayName("Lanza conflicto cuando la nueva palabra coincide con otro registro con distinta capitalización")
    void updateShouldThrowConflictWhenNewWordMatchesAnotherRecordWithDifferentCase() {
        RiskWord existing = existingWord(1L, "ansiedad", RiskSeverity.LOW);
        givenFound(1L, existing);
        givenWordTakenByAnother("panico", 1L);

        thenUpdateThrowsConflict(1L, wordOnly("PANICO"), "panico");
    }

    @Test
    @DisplayName("Omite la verificación de duplicados cuando la palabra no cambió")
    void updateShouldSkipDuplicateCheckWhenWordIsUnchanged() {
        RiskWord existing = existingWord(1L, "ansiedad", RiskSeverity.LOW);
        givenFound(1L, existing);
        givenSavedAny(existing);

        update(1L, word("ansiedad", RiskSeverity.HIGH));

        thenDuplicateCheckSkipped();
    }

    @Test
    @DisplayName("Omite la verificación de duplicados cuando la palabra no cambió con distinta capitalización")
    void updateShouldSkipDuplicateCheckWhenWordIsUnchangedDifferentCase() {
        RiskWord existing = existingWord(1L, "ansiedad", RiskSeverity.LOW);
        givenFound(1L, existing);
        givenSavedAny(existing);

        update(1L, word("ANSIEDAD", RiskSeverity.HIGH));

        thenDuplicateCheckSkipped();
    }

    @Test
    @DisplayName("Elimina por id cuando el id existe")
    void deleteShouldCallDeleteByIdWhenIdExists() {
        givenIdExists(1L);

        delete(1L);

        thenDeleted(1L);
    }

    @Test
    @DisplayName("Lanza no encontrado cuando el id no existe al eliminar")
    void deleteShouldThrowNotFoundWhenIdDoesNotExist() {
        givenIdDoesNotExist(99L);

        thenDeleteThrowsNotFound(99L, "99");
        thenDeleteNeverInvoked();
    }

    @Test
    @DisplayName("Devuelve la página cuando los filtros son válidos")
    void listShouldReturnPageWhenFiltersAreValid() {
        Pageable pageable = pageable();
        givenListReturns(null, null, null, pageable);

        Page<RiskWord> result = list(null, null, null, pageable);

        thenPageNotNull(result);
    }

    @Test
    @DisplayName("Devuelve la página cuando la severidad es válida")
    void listShouldReturnPageWhenSeverityIsValid() {
        Pageable pageable = pageable();
        givenListReturns(null, null, "HIGH", pageable);

        Page<RiskWord> result = list(null, null, "HIGH", pageable);

        thenPageNotNull(result);
        thenListedWith(null, null, "HIGH", pageable);
    }

    @Test
    @DisplayName("Devuelve la página cuando la severidad está en blanco")
    void listShouldReturnPageWhenSeverityIsBlank() {
        Pageable pageable = pageable();
        givenListReturns(null, null, "", pageable);

        Page<RiskWord> result = list(null, null, "", pageable);

        thenPageNotNull(result);
        thenListedWith(null, null, "", pageable);
    }

    @Test
    @DisplayName("Lanza solicitud inválida cuando la severidad es inválida")
    void listShouldThrowBadRequestWhenSeverityIsInvalid() {
        thenListThrowsBadRequest(null, null, "INVALIDA", pageable(), "LOW, MEDIUM, HIGH");
    }

    // --- arrange ---
    private void givenWordDoesNotExist(String word) {
        when(riskWordRepository.existsByWordIgnoreCase(word)).thenReturn(false);
    }

    private void givenWordAlreadyExists(String word) {
        when(riskWordRepository.existsByWordIgnoreCase(word)).thenReturn(true);
    }

    private void givenSaved(RiskWord expectedArgument, RiskWord result) {
        when(riskWordRepository.save(expectedArgument)).thenReturn(result);
    }

    private void givenSavedAny(RiskWord result) {
        when(riskWordRepository.save(any())).thenReturn(result);
    }

    private void givenFound(Long id, RiskWord entity) {
        when(riskWordRepository.findById(id)).thenReturn(Optional.of(entity));
    }

    private void givenNotFound(Long id) {
        when(riskWordRepository.findById(id)).thenReturn(Optional.empty());
    }

    private void givenWordNotTakenByAnother(String word, Long id) {
        when(riskWordRepository.existsByWordIgnoreCaseAndIdNot(word, id)).thenReturn(false);
    }

    private void givenWordTakenByAnother(String word, Long id) {
        when(riskWordRepository.existsByWordIgnoreCaseAndIdNot(word, id)).thenReturn(true);
    }

    private void givenIdExists(Long id) {
        when(riskWordRepository.existsById(id)).thenReturn(true);
    }

    private void givenIdDoesNotExist(Long id) {
        when(riskWordRepository.existsById(id)).thenReturn(false);
    }

    private void givenListReturns(String word, Boolean active, String severity, Pageable pageable) {
        when(riskWordRepository.findAll(word, active, severity, pageable)).thenReturn(emptyPage());
    }

    private RiskWord word(String value, RiskSeverity severity) {
        return RiskWord.builder().word(value).severity(severity).active(true).build();
    }

    private RiskWord savedWord(Long id, String value, RiskSeverity severity) {
        return RiskWord.builder().id(id).word(value).severity(severity).active(true).build();
    }

    private RiskWord existingWord(Long id, String value, RiskSeverity severity) {
        return RiskWord.builder().id(id).word(value).severity(severity).active(true).build();
    }

    private RiskWord patch(String value, String description, RiskSeverity severity) {
        return RiskWord.builder().word(value).description(description).severity(severity).build();
    }

    private RiskWord wordOnly(String value) {
        return RiskWord.builder().word(value).build();
    }

    private Pageable pageable() {
        return PageRequest.of(0, 20);
    }

    private Page<RiskWord> emptyPage() {
        return new PageImpl<>(List.of());
    }

    // --- act ---
    private RiskWord create(RiskWord input) {
        return riskWordService.create(input);
    }

    private RiskWord update(Long id, RiskWord patch) {
        return riskWordService.update(id, patch);
    }

    private void delete(Long id) {
        riskWordService.delete(id);
    }

    private Page<RiskWord> list(String word, Boolean active, String severity, Pageable pageable) {
        return riskWordService.list(word, active, severity, pageable);
    }

    // --- assert ---
    private void thenResultId(RiskWord result, Long id) {
        assertThat(result.getId()).isEqualTo(id);
    }

    private void thenSaved(RiskWord entity) {
        verify(riskWordRepository).save(entity);
    }

    private void thenSaveNeverInvoked() {
        verify(riskWordRepository, never()).save(any());
    }

    private void thenWordIs(RiskWord entity, String expected) {
        assertThat(entity.getWord()).isEqualTo(expected);
    }

    private void thenSeverityIs(RiskWord entity, RiskSeverity severity) {
        assertThat(entity.getSeverity()).isEqualTo(severity);
    }

    private void thenUniquenessCheckedFor(String word) {
        verify(riskWordRepository).existsByWordIgnoreCase(word);
    }

    private void thenDuplicateCheckSkipped() {
        verify(riskWordRepository, never()).existsByWordIgnoreCaseAndIdNot(any(), any());
    }

    private void thenDeleted(Long id) {
        verify(riskWordRepository).deleteById(id);
    }

    private void thenDeleteNeverInvoked() {
        verify(riskWordRepository, never()).deleteById(any());
    }

    private void thenPageNotNull(Page<RiskWord> result) {
        assertThat(result).isNotNull();
    }

    private void thenListedWith(String word, Boolean active, String severity, Pageable pageable) {
        verify(riskWordRepository).findAll(word, active, severity, pageable);
    }

    private void thenCreateThrowsConflict(RiskWord input, String messageFragment) {
        assertThatThrownBy(() -> riskWordService.create(input))
                .isInstanceOf(ConflictException.class)
                .hasMessageContaining(messageFragment);
    }

    private void thenUpdateThrowsNotFound(Long id, RiskWord patch, String messageFragment) {
        assertThatThrownBy(() -> riskWordService.update(id, patch))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining(messageFragment);
    }

    private void thenUpdateThrowsConflict(Long id, RiskWord patch, String messageFragment) {
        assertThatThrownBy(() -> riskWordService.update(id, patch))
                .isInstanceOf(ConflictException.class)
                .hasMessageContaining(messageFragment);
    }

    private void thenDeleteThrowsNotFound(Long id, String messageFragment) {
        assertThatThrownBy(() -> riskWordService.delete(id))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining(messageFragment);
    }

    private void thenListThrowsBadRequest(
            String word, Boolean active, String severity, Pageable pageable, String messageFragment) {
        assertThatThrownBy(() -> riskWordService.list(word, active, severity, pageable))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining(messageFragment);
    }
}
