package com.huly.backend.infrastructure.repository.jpaRepository.implementation;

import com.huly.backend.domain.model.pending.PendingSubtask;
import com.huly.backend.infrastructure.presentation.exception.NotFoundException;
import com.huly.backend.infrastructure.repository.entity.PendingSubtaskEntity;
import com.huly.backend.infrastructure.repository.entity.PendingTaskEntity;
import com.huly.backend.infrastructure.repository.jpaRepository.interfaces.IPendingSubtaskJpaRepository;
import com.huly.backend.infrastructure.repository.jpaRepository.interfaces.IPendingTaskJpaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PendingSubtaskRepositoryImplTest {

    @Mock
    private IPendingSubtaskJpaRepository jpaRepository;

    @Mock
    private IPendingTaskJpaRepository taskJpaRepository;

    private PendingSubtaskRepositoryImpl repository;

    @BeforeEach
    void setUp() {
        repository = new PendingSubtaskRepositoryImpl(jpaRepository, taskJpaRepository);
    }

    @Test
    @DisplayName("Crea y guarda una subtarea para una tarea padre existente")
    void createShouldCreateSubtask() {
        PendingTaskEntity task = buildTaskEntity(1L);
        givenTaskExists(1L, task);

        PendingSubtaskEntity savedSubtask = buildSubtaskEntity(100L, task, "Subtarea", false);
        givenSubtaskSaved(savedSubtask);

        PendingSubtask result = performCreate(1L, "Subtarea", 0);

        thenSubtaskCreatedCorrectly(result, 100L, "Subtarea");
    }

    @Test
    @DisplayName("Lanza excepción al intentar crear una subtarea si la tarea padre no existe")
    void createShouldThrowNotFoundWhenTaskDoesNotExist() {
        givenTaskDoesNotExist(1L);

        assertThatThrownBy(() -> performCreate(1L, "Subtarea", 0))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    @DisplayName("Busca una subtarea por ID e ID de la tarea padre")
    void findByIdAndTaskIdShouldReturnSubtask() {
        PendingTaskEntity task = buildTaskEntity(1L);
        PendingSubtaskEntity entity = buildSubtaskEntity(100L, task, "Subtarea", false);
        givenFindByIdAndTaskIdReturns(100L, 1L, entity);

        Optional<PendingSubtask> result = performFindByIdAndTaskId(100L, 1L);

        thenFindResultContainsId(result, 100L);
    }

    @Test
    @DisplayName("Invierte el valor del estado completado de una subtarea existente")
    void toggleShouldToggleCompletedStatus() {
        PendingTaskEntity task = buildTaskEntity(1L);
        PendingSubtaskEntity entity = buildSubtaskEntity(100L, task, "Subtarea", false);
        givenFindByIdReturns(100L, entity);
        givenSubtaskSaved(entity);

        PendingSubtask result = performToggle(100L);

        thenSubtaskIsDone(result, true);
    }

    @Test
    @DisplayName("Lanza excepción al alternar el estado de una subtarea inexistente")
    void toggleShouldThrowNotFoundWhenSubtaskDoesNotExist() {
        givenFindByIdReturnsEmpty(100L);

        assertThatThrownBy(() -> performToggle(100L))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    @DisplayName("Elimina una subtarea por ID")
    void deleteShouldDeleteSubtask() {
        performDelete(100L);

        thenJpaDeleteCalled(100L);
    }

    @Test
    @DisplayName("Cuenta la cantidad de subtareas asociadas a una tarea padre")
    void countByTaskIdShouldReturnCount() {
        givenCountByTaskIdReturns(1L, 5);

        int count = performCountByTaskId(1L);

        thenCountIs(count, 5);
    }

    // --- arrange ---

    private void givenTaskExists(Long id, PendingTaskEntity task) {
        when(taskJpaRepository.findById(id)).thenReturn(Optional.of(task));
    }

    private void givenTaskDoesNotExist(Long id) {
        when(taskJpaRepository.findById(id)).thenReturn(Optional.empty());
    }

    private void givenSubtaskSaved(PendingSubtaskEntity entity) {
        when(jpaRepository.save(any(PendingSubtaskEntity.class))).thenReturn(entity);
    }

    private void givenFindByIdAndTaskIdReturns(Long subtaskId, Long taskId, PendingSubtaskEntity entity) {
        when(jpaRepository.findByIdAndTask_Id(subtaskId, taskId)).thenReturn(Optional.of(entity));
    }

    private void givenFindByIdReturns(Long id, PendingSubtaskEntity entity) {
        when(jpaRepository.findById(id)).thenReturn(Optional.of(entity));
    }

    private void givenFindByIdReturnsEmpty(Long id) {
        when(jpaRepository.findById(id)).thenReturn(Optional.empty());
    }

    private void givenCountByTaskIdReturns(Long taskId, int count) {
        when(jpaRepository.countByTask_Id(taskId)).thenReturn(count);
    }

    // --- act ---

    private PendingSubtask performCreate(Long taskId, String text, int position) {
        return repository.create(taskId, text, position);
    }

    private Optional<PendingSubtask> performFindByIdAndTaskId(Long subtaskId, Long taskId) {
        return repository.findByIdAndTaskId(subtaskId, taskId);
    }

    private PendingSubtask performToggle(Long subtaskId) {
        return repository.toggle(subtaskId);
    }

    private void performDelete(Long subtaskId) {
        repository.delete(subtaskId);
    }

    private int performCountByTaskId(Long taskId) {
        return repository.countByTaskId(taskId);
    }

    // --- assert ---

    private void thenSubtaskCreatedCorrectly(PendingSubtask result, Long expectedId, String expectedText) {
        assertThat(result.getId()).isEqualTo(expectedId);
        assertThat(result.getText()).isEqualTo(expectedText);
    }

    private void thenFindResultContainsId(Optional<PendingSubtask> result, Long expectedId) {
        assertThat(result).isPresent();
        assertThat(result.get().getId()).isEqualTo(expectedId);
    }

    private void thenSubtaskIsDone(PendingSubtask result, boolean expected) {
        assertThat(result.isDone()).isEqualTo(expected);
    }

    private void thenJpaDeleteCalled(Long subtaskId) {
        verify(jpaRepository).deleteById(subtaskId);
    }

    private void thenCountIs(int actual, int expected) {
        assertThat(actual).isEqualTo(expected);
    }

    // --- helpers ---

    private PendingTaskEntity buildTaskEntity(Long id) {
        return PendingTaskEntity.builder().id(id).build();
    }

    private PendingSubtaskEntity buildSubtaskEntity(Long id, PendingTaskEntity task, String text, boolean done) {
        return PendingSubtaskEntity.builder()
                .id(id)
                .task(task)
                .text(text)
                .done(done)
                .position(0)
                .build();
    }
}
