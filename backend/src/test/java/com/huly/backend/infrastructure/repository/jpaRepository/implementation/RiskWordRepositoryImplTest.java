package com.huly.backend.infrastructure.repository.jpaRepository.implementation;

import com.huly.backend.domain.model.enums.RiskSeverity;
import com.huly.backend.domain.model.riskWord.RiskWord;
import com.huly.backend.infrastructure.repository.entity.RiskWordEntity;
import com.huly.backend.infrastructure.repository.jpaRepository.interfaces.IRiskWordJpaRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RiskWordRepositoryImplTest {

    private static final Long WORD_ID = 1L;
    private static final Long SAVED_ID = 42L;
    private static final Long MISSING_ID = 99L;
    private static final String WORD_SUICIDIO = "suicidio";
    private static final String WORD_ANSIEDAD = "ansiedad";
    private static final Pageable PAGEABLE = PageRequest.of(0, 20);

    @Mock
    private IRiskWordJpaRepository jpa;

    @InjectMocks
    private RiskWordRepositoryImpl repositoryImpl;

    @Test
    @DisplayName("Mapea el dominio a entidad antes de persistir con save")
    void saveShouldMapDomainToEntityBeforePersisting() {
        givenSaved(riskWordEntity(WORD_ID, WORD_SUICIDIO, RiskSeverity.HIGH, true));

        save(riskWordDomain(WORD_SUICIDIO, RiskSeverity.HIGH, true));

        thenPersistedRiskWord(WORD_SUICIDIO, RiskSeverity.HIGH, true);
    }

    @Test
    @DisplayName("Mapea la entidad guardada a dominio con save")
    void saveShouldMapEntityToDomainAfterPersisting() {
        givenSaved(riskWordEntity(SAVED_ID, WORD_SUICIDIO, RiskSeverity.HIGH, true));

        RiskWord result = save(riskWordDomain(WORD_SUICIDIO, RiskSeverity.HIGH, true));

        thenRiskWordMapped(result, SAVED_ID, WORD_SUICIDIO, RiskSeverity.HIGH);
    }

    @Test
    @DisplayName("Devuelve el dominio mapeado por id cuando la entidad existe")
    void findByIdShouldReturnMappedDomainWhenEntityExists() {
        givenFindById(WORD_ID, riskWordEntity(WORD_ID, WORD_ANSIEDAD, RiskSeverity.MEDIUM, true));

        Optional<RiskWord> result = findById(WORD_ID);

        thenPresentWithWord(result, WORD_ANSIEDAD);
    }

    @Test
    @DisplayName("Devuelve vacío al buscar por id cuando la entidad no existe")
    void findByIdShouldReturnEmptyWhenEntityDoesNotExist() {
        givenFindByIdMissing(MISSING_ID);

        Optional<RiskWord> result = findById(MISSING_ID);

        thenAbsent(result);
    }

    @Test
    @DisplayName("Delega la eliminación por id al repositorio JPA")
    void deleteByIdShouldDelegateToJpa() {
        deleteById(WORD_ID);

        thenDeleted(WORD_ID);
    }

    @Test
    @DisplayName("Devuelve true cuando existe la entidad por id")
    void existsByIdShouldReturnTrueWhenEntityExists() {
        givenExists(WORD_ID, true);

        boolean result = existsById(WORD_ID);

        thenExists(result, true);
    }

    @Test
    @DisplayName("Devuelve false cuando no existe la entidad por id")
    void existsByIdShouldReturnFalseWhenEntityDoesNotExist() {
        givenExists(MISSING_ID, false);

        boolean result = existsById(MISSING_ID);

        thenExists(result, false);
    }

    @Test
    @DisplayName("Delega la verificación de existencia por palabra al repositorio JPA")
    void existsByWordIgnoreCaseShouldDelegateToJpa() {
        givenExistsByWord(WORD_SUICIDIO, true);

        boolean result = existsByWord(WORD_SUICIDIO);

        thenExists(result, true);
    }

    @Test
    @DisplayName("Delega la verificación de existencia por palabra excluyendo id al repositorio JPA")
    void existsByWordIgnoreCaseAndIdNotShouldDelegateToJpa() {
        givenExistsByWordAndIdNot(WORD_SUICIDIO, WORD_ID, false);

        boolean result = existsByWordAndIdNot(WORD_SUICIDIO, WORD_ID);

        thenExists(result, false);
    }

    @Test
    @DisplayName("Devuelve la página filtrada mapeada a dominio")
    void findAllShouldReturnMappedPage() {
        givenFilteredPage(riskWordEntity(WORD_ID, WORD_SUICIDIO, RiskSeverity.HIGH, true));

        Page<RiskWord> result = findAll(null, null, null);

        thenPageContainsWord(result, WORD_SUICIDIO);
    }

    @Test
    @DisplayName("Mapea todas las palabras activas")
    void findAllActiveShouldMapEntities() {
        givenActiveWords(
                riskWordEntity(WORD_ID, WORD_SUICIDIO, RiskSeverity.HIGH, true),
                riskWordEntity(2L, WORD_ANSIEDAD, RiskSeverity.MEDIUM, true));

        List<RiskWord> result = findAllActive();

        thenActiveWordsAre(result, WORD_SUICIDIO, WORD_ANSIEDAD);
    }

    @Test
    @DisplayName("Devuelve lista vacía cuando no hay palabras activas")
    void findAllActiveShouldReturnEmptyWhenNone() {
        givenActiveWords();

        List<RiskWord> result = findAllActive();

        thenActiveEmpty(result);
    }

    // --- arrange ---
    private void givenSaved(RiskWordEntity entity) {
        when(jpa.save(any(RiskWordEntity.class))).thenReturn(entity);
    }

    private void givenFindById(Long id, RiskWordEntity entity) {
        when(jpa.findById(id)).thenReturn(Optional.of(entity));
    }

    private void givenFindByIdMissing(Long id) {
        when(jpa.findById(id)).thenReturn(Optional.empty());
    }

    private void givenExists(Long id, boolean exists) {
        when(jpa.existsById(id)).thenReturn(exists);
    }

    private void givenExistsByWord(String word, boolean exists) {
        when(jpa.existsByWordIgnoreCase(word)).thenReturn(exists);
    }

    private void givenExistsByWordAndIdNot(String word, Long id, boolean exists) {
        when(jpa.existsByWordIgnoreCaseAndIdNot(word, id)).thenReturn(exists);
    }

    @SuppressWarnings("unchecked")
    private void givenFilteredPage(RiskWordEntity entity) {
        when(jpa.findAll(any(Specification.class), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(entity)));
    }

    private void givenActiveWords(RiskWordEntity... entities) {
        when(jpa.findByActiveTrue()).thenReturn(List.of(entities));
    }

    private RiskWord riskWordDomain(String word, RiskSeverity severity, boolean active) {
        return RiskWord.builder().word(word).severity(severity).active(active).build();
    }

    private RiskWordEntity riskWordEntity(Long id, String word, RiskSeverity severity, boolean active) {
        return RiskWordEntity.builder().id(id).word(word).severity(severity).active(active).build();
    }

    // --- act ---
    private RiskWord save(RiskWord domain) {
        return repositoryImpl.save(domain);
    }

    private Optional<RiskWord> findById(Long id) {
        return repositoryImpl.findById(id);
    }

    private void deleteById(Long id) {
        repositoryImpl.deleteById(id);
    }

    private boolean existsById(Long id) {
        return repositoryImpl.existsById(id);
    }

    private boolean existsByWord(String word) {
        return repositoryImpl.existsByWordIgnoreCase(word);
    }

    private boolean existsByWordAndIdNot(String word, Long id) {
        return repositoryImpl.existsByWordIgnoreCaseAndIdNot(word, id);
    }

    private Page<RiskWord> findAll(String word, Boolean active, String severity) {
        return repositoryImpl.findAll(word, active, severity, PAGEABLE);
    }

    private List<RiskWord> findAllActive() {
        return repositoryImpl.findAllActive();
    }

    // --- assert ---
    private void thenPersistedRiskWord(String word, RiskSeverity severity, boolean active) {
        ArgumentCaptor<RiskWordEntity> captor = ArgumentCaptor.forClass(RiskWordEntity.class);
        verify(jpa).save(captor.capture());
        RiskWordEntity captured = captor.getValue();
        assertThat(captured.getWord()).isEqualTo(word);
        assertThat(captured.getSeverity()).isEqualTo(severity);
        assertThat(captured.isActive()).isEqualTo(active);
    }

    private void thenRiskWordMapped(RiskWord result, Long id, String word, RiskSeverity severity) {
        assertThat(result.getId()).isEqualTo(id);
        assertThat(result.getWord()).isEqualTo(word);
        assertThat(result.getSeverity()).isEqualTo(severity);
    }

    private void thenPresentWithWord(Optional<RiskWord> result, String word) {
        assertThat(result).isPresent();
        assertThat(result.get().getWord()).isEqualTo(word);
    }

    private void thenAbsent(Optional<RiskWord> result) {
        assertThat(result).isEmpty();
    }

    private void thenDeleted(Long id) {
        verify(jpa).deleteById(id);
    }

    private void thenExists(boolean result, boolean expected) {
        assertThat(result).isEqualTo(expected);
    }

    private void thenPageContainsWord(Page<RiskWord> result, String word) {
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getWord()).isEqualTo(word);
    }

    private void thenActiveWordsAre(List<RiskWord> result, String... words) {
        assertThat(result).extracting(RiskWord::getWord).containsExactly(words);
    }

    private void thenActiveEmpty(List<RiskWord> result) {
        assertThat(result).isEmpty();
    }
}
