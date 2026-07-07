package com.huly.backend.infrastructure.repository.jpaRepository.implementation;

import com.huly.backend.domain.model.enums.EstimatedDuration;
import com.huly.backend.domain.model.enums.MentalLoadBucket;
import com.huly.backend.domain.model.enums.PendingCategory;
import com.huly.backend.domain.model.enums.PendingStatus;
import com.huly.backend.domain.model.pending.PendingTask;
import com.huly.backend.infrastructure.presentation.exception.NotFoundException;
import com.huly.backend.infrastructure.repository.entity.AppUserEntity;
import com.huly.backend.infrastructure.repository.entity.PendingTaskEntity;
import com.huly.backend.infrastructure.repository.jpaRepository.interfaces.AppUserRepository;
import com.huly.backend.infrastructure.repository.jpaRepository.interfaces.IPendingTaskJpaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PendingTaskRepositoryImplTest {

    @Mock
    private IPendingTaskJpaRepository jpaRepository;

    @Mock
    private AppUserRepository appUserRepository;

    private PendingTaskRepositoryImpl repository;

    @BeforeEach
    void setUp() {
        repository = new PendingTaskRepositoryImpl(jpaRepository, appUserRepository);
    }

    @Test
    @DisplayName("Crea una tarea pendiente y sus subtareas en la base de datos")
    void createShouldCreateTaskAndSubtasks() {
        AppUserEntity user = buildUser(10L);
        givenUserExists(10L, user);

        PendingTaskEntity savedEntity = buildTaskEntity(1L, user, "Lavar platos", PendingStatus.PENDING);
        givenTaskSaved(savedEntity);

        PendingTask task = performCreate(10L, "Lavar platos", EstimatedDuration.FIFTEEN_MIN, PendingCategory.HOGAR);

        thenTaskIsCreatedCorrectly(task, 1L, 10L, "Lavar platos");
    }

    @Test
    @DisplayName("Busca una tarea pendiente por ID y ID de usuario")
    void findByIdAndUserIdShouldReturnTask() {
        AppUserEntity user = buildUser(10L);
        PendingTaskEntity entity = buildTaskEntity(1L, user, "Tarea", PendingStatus.PENDING);
        givenFindByIdAndUserIdReturns(1L, 10L, entity);

        Optional<PendingTask> result = performFindByIdAndUserId(1L, 10L);

        thenFindResultContainsId(result, 1L);
    }

    @Test
    @DisplayName("Busca todas las tareas del usuario filtrando por estado de forma correcta")
    void findAllByUserIdShouldFilterByStatus() {
        AppUserEntity user = buildUser(10L);
        PendingTaskEntity entity = buildTaskEntity(1L, user, "Tarea", PendingStatus.PENDING);
        givenFindAllByStatusReturns(10L, PendingStatus.PENDING, List.of(entity));

        List<PendingTask> results = performFindAllByUserId(10L, PendingStatus.PENDING);

        thenResultsSizeIs(results, 1);
    }

    @Test
    @DisplayName("Busca todas las tareas del usuario sin filtrar por estado cuando el filtro es nulo")
    void findAllByUserIdShouldNotFilterByStatusWhenNull() {
        AppUserEntity user = buildUser(10L);
        PendingTaskEntity entity = buildTaskEntity(1L, user, "Tarea", PendingStatus.PENDING);
        givenFindAllReturns(10L, List.of(entity));

        List<PendingTask> results = performFindAllByUserId(10L, null);

        thenResultsSizeIs(results, 1);
    }

    @Test
    @DisplayName("Busca únicamente las tareas con estado PENDING de un usuario")
    void findPendingByUserIdShouldReturnPendingTasks() {
        AppUserEntity user = buildUser(10L);
        PendingTaskEntity entity = buildTaskEntity(1L, user, "Tarea", PendingStatus.PENDING);
        givenFindPendingReturns(10L, List.of(entity));

        List<PendingTask> results = performFindPendingByUserId(10L);

        thenResultsSizeIs(results, 1);
    }

    @Test
    @DisplayName("Elimina una tarea existente que pertenezca al usuario")
    void deleteShouldDeleteTaskWhenOwned() {
        AppUserEntity user = buildUser(10L);
        PendingTaskEntity entity = buildTaskEntity(1L, user, "Tarea", PendingStatus.PENDING);
        givenFindByIdAndUserIdReturns(1L, 10L, entity);

        performDelete(1L, 10L);

        thenJpaDeleteCalled(entity);
    }

    @Test
    @DisplayName("Lanza excepción si la tarea a eliminar no existe o pertenece a otro usuario")
    void deleteShouldThrowNotFoundWhenNotOwnedOrNotExists() {
        givenFindByIdAndUserIdReturnsEmpty(1L, 10L);

        assertThatThrownBy(() -> performDelete(1L, 10L))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    @DisplayName("Modifica los campos editables de la tarea persistida")
    void updateFieldsShouldModifyFields() {
        AppUserEntity user = buildUser(10L);
        PendingTaskEntity entity = buildTaskEntity(1L, user, "Original", PendingStatus.PENDING);
        givenFindByIdReturns(1L, entity);
        givenTaskSaved(entity);

        PendingTask result = performUpdateFields(1L, "Nuevo");

        thenTaskTitleIs(result, "Nuevo");
    }

    @Test
    @DisplayName("Actualiza la carga mental calculada de la tarea")
    void updateMentalLoadShouldSetMentalLoad() {
        AppUserEntity user = buildUser(10L);
        PendingTaskEntity entity = buildTaskEntity(1L, user, "Tarea", PendingStatus.PENDING);
        givenFindByIdReturns(1L, entity);
        givenTaskSaved(entity);

        PendingTask result = performUpdateMentalLoad(1L, 0.5, MentalLoadBucket.MEDIUM);

        thenTaskMentalLoadIs(result, 0.5, MentalLoadBucket.MEDIUM);
    }

    @Test
    @DisplayName("Marca una tarea pendiente como COMPLETADA")
    void markCompletedShouldMarkCompleted() {
        AppUserEntity user = buildUser(10L);
        PendingTaskEntity entity = buildTaskEntity(1L, user, "Tarea", PendingStatus.PENDING);
        givenFindByIdReturns(1L, entity);
        givenTaskSaved(entity);

        Instant completedAt = Instant.now();
        PendingTask result = performMarkCompleted(1L, completedAt);

        thenTaskStatusIs(result, PendingStatus.COMPLETED);
    }

    @Test
    @DisplayName("Actualiza las coordenadas posicionales y de rotación en el tablero")
    void updatePositionShouldSetPosition() {
        AppUserEntity user = buildUser(10L);
        PendingTaskEntity entity = buildTaskEntity(1L, user, "Tarea", PendingStatus.PENDING);
        givenFindByIdReturns(1L, entity);
        givenTaskSaved(entity);

        Instant now = Instant.now();
        PendingTask result = performUpdatePosition(1L, 10.0, 20.0, 4.5, now);

        thenTaskPositionIs(result, 10.0, 20.0, 4.5);
    }

    // --- arrange ---

    private void givenUserExists(Long id, AppUserEntity user) {
        when(appUserRepository.getReferenceById(id)).thenReturn(user);
    }

    private void givenTaskSaved(PendingTaskEntity entity) {
        when(jpaRepository.save(any(PendingTaskEntity.class))).thenReturn(entity);
    }

    private void givenFindByIdAndUserIdReturns(Long id, Long userId, PendingTaskEntity entity) {
        when(jpaRepository.findByIdAndUser_Id(id, userId)).thenReturn(Optional.of(entity));
    }

    private void givenFindByIdAndUserIdReturnsEmpty(Long id, Long userId) {
        when(jpaRepository.findByIdAndUser_Id(id, userId)).thenReturn(Optional.empty());
    }

    private void givenFindAllByStatusReturns(Long userId, PendingStatus status, List<PendingTaskEntity> list) {
        when(jpaRepository.findAllByUser_IdAndStatusOrderByCreatedAtDesc(userId, status)).thenReturn(list);
    }

    private void givenFindAllReturns(Long userId, List<PendingTaskEntity> list) {
        when(jpaRepository.findAllByUser_IdOrderByCreatedAtDesc(userId)).thenReturn(list);
    }

    private void givenFindPendingReturns(Long userId, List<PendingTaskEntity> list) {
        when(jpaRepository.findAllByUser_IdAndStatus(userId, PendingStatus.PENDING)).thenReturn(list);
    }

    private void givenFindByIdReturns(Long id, PendingTaskEntity entity) {
        when(jpaRepository.findById(id)).thenReturn(Optional.of(entity));
    }

    // --- act ---

    private PendingTask performCreate(Long userId, String title, EstimatedDuration duration, PendingCategory category) {
        return repository.create(userId, title, "Desc", LocalDate.now(), duration, category, List.of("Subtask 1"));
    }

    private Optional<PendingTask> performFindByIdAndUserId(Long id, Long userId) {
        return repository.findByIdAndUserId(id, userId);
    }

    private List<PendingTask> performFindAllByUserId(Long userId, PendingStatus status) {
        return repository.findAllByUserId(userId, status);
    }

    private List<PendingTask> performFindPendingByUserId(Long userId) {
        return repository.findPendingByUserId(userId);
    }

    private void performDelete(Long id, Long userId) {
        repository.delete(id, userId);
    }

    private PendingTask performUpdateFields(Long id, String title) {
        return repository.updateFields(id, title, "Desc", null, null, null);
    }

    private PendingTask performUpdateMentalLoad(Long id, double score, MentalLoadBucket bucket) {
        return repository.updateMentalLoad(id, score, bucket);
    }

    private PendingTask performMarkCompleted(Long id, Instant completedAt) {
        return repository.markCompleted(id, completedAt);
    }

    private PendingTask performUpdatePosition(Long id, double x, double y, Double rotation, Instant now) {
        return repository.updatePosition(id, x, y, rotation, now);
    }

    // --- assert ---

    private void thenTaskIsCreatedCorrectly(PendingTask task, Long expectedId, Long expectedUserId, String expectedTitle) {
        assertThat(task.getId()).isEqualTo(expectedId);
        assertThat(task.getUserId()).isEqualTo(expectedUserId);
        assertThat(task.getTitle()).isEqualTo(expectedTitle);
    }

    private void thenFindResultContainsId(Optional<PendingTask> result, Long expectedId) {
        assertThat(result).isPresent();
        assertThat(result.get().getId()).isEqualTo(expectedId);
    }

    private void thenResultsSizeIs(List<PendingTask> results, int size) {
        assertThat(results).hasSize(size);
    }

    private void thenJpaDeleteCalled(PendingTaskEntity entity) {
        verify(jpaRepository).delete(entity);
    }

    private void thenTaskTitleIs(PendingTask result, String expectedTitle) {
        assertThat(result.getTitle()).isEqualTo(expectedTitle);
    }

    private void thenTaskMentalLoadIs(PendingTask result, double score, MentalLoadBucket bucket) {
        assertThat(result.getMentalLoadScore()).isEqualTo(score);
        assertThat(result.getMentalLoadBucket()).isEqualTo(bucket);
    }

    private void thenTaskStatusIs(PendingTask result, PendingStatus status) {
        assertThat(result.getStatus()).isEqualTo(status);
    }

    private void thenTaskPositionIs(PendingTask result, double x, double y, Double rotation) {
        assertThat(result.getPositionX()).isEqualTo(x);
        assertThat(result.getPositionY()).isEqualTo(y);
        assertThat(result.getRotationDeg()).isEqualTo(rotation);
    }

    // --- helpers ---

    private AppUserEntity buildUser(Long id) {
        AppUserEntity user = new AppUserEntity();
        user.setId(id);
        return user;
    }

    private PendingTaskEntity buildTaskEntity(Long id, AppUserEntity user, String title, PendingStatus status) {
        return PendingTaskEntity.builder()
                .id(id)
                .user(user)
                .title(title)
                .status(status)
                .subtasks(Collections.emptyList())
                .build();
    }
}
