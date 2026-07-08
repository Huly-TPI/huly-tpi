package com.huly.backend.infrastructure.repository.jpaRepository.implementation;

import com.huly.backend.domain.model.enums.RecommendationResponseDecision;
import com.huly.backend.domain.model.pending.PendingDailyRecommendation;
import com.huly.backend.infrastructure.presentation.exception.NotFoundException;
import com.huly.backend.infrastructure.repository.entity.AppUserEntity;
import com.huly.backend.infrastructure.repository.entity.PendingDailyRecommendationEntity;
import com.huly.backend.infrastructure.repository.entity.PendingDailyRecommendationTaskEntity;
import com.huly.backend.infrastructure.repository.entity.PendingTaskEntity;
import com.huly.backend.infrastructure.repository.jpaRepository.interfaces.AppUserRepository;
import com.huly.backend.infrastructure.repository.jpaRepository.interfaces.IPendingDailyRecommendationJpaRepository;
import com.huly.backend.infrastructure.repository.jpaRepository.interfaces.IPendingDailyRecommendationTaskJpaRepository;
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
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PendingRecommendationRepositoryImplTest {

    @Mock
    private IPendingDailyRecommendationJpaRepository jpaRepository;

    @Mock
    private IPendingDailyRecommendationTaskJpaRepository taskJoinJpaRepository;

    @Mock
    private IPendingTaskJpaRepository taskJpaRepository;

    @Mock
    private AppUserRepository appUserRepository;

    private PendingRecommendationRepositoryImpl repository;

    @BeforeEach
    void setUp() {
        repository = new PendingRecommendationRepositoryImpl(
                jpaRepository,
                taskJoinJpaRepository,
                taskJpaRepository,
                appUserRepository
        );
    }

    @Test
    @DisplayName("Busca la recomendación diaria de un usuario por fecha y carga sus IDs recomendados")
    void findByUserIdAndDateShouldReturnRecommendation() {
        AppUserEntity user = buildUser(10L);
        PendingDailyRecommendationEntity entity = buildRecommendationEntity(1L, user, LocalDate.now(), RecommendationResponseDecision.PENDING);

        givenRecommendationByDateReturns(10L, LocalDate.now(), entity);
        givenTaskJoinsReturns(1L, Collections.emptyList());

        Optional<PendingDailyRecommendation> result = performFindByUserIdAndDate(10L, LocalDate.now());

        thenResultContainsId(result, 1L);
    }

    @Test
    @DisplayName("Busca una recomendación diaria por ID e ID del usuario")
    void findByIdAndUserIdShouldReturnRecommendation() {
        AppUserEntity user = buildUser(10L);
        PendingDailyRecommendationEntity entity = buildRecommendationEntity(1L, user, LocalDate.now(), RecommendationResponseDecision.PENDING);

        givenRecommendationByIdAndUserReturns(1L, 10L, entity);
        givenTaskJoinsReturns(1L, Collections.emptyList());

        Optional<PendingDailyRecommendation> result = performFindByIdAndUserId(1L, 10L);

        thenResultContainsId(result, 1L);
    }

    @Test
    @DisplayName("Crea (upsert) una nueva recomendación diaria y asocia sus tareas sugeridas")
    void upsertShouldCreateNewRecommendation() {
        AppUserEntity user = buildUser(10L);
        givenUserExists(10L, user);
        givenRecommendationByDateReturnsEmpty(10L, LocalDate.now());

        PendingDailyRecommendationEntity entity = buildRecommendationEntity(1L, user, LocalDate.now(), RecommendationResponseDecision.PENDING);
        givenRecommendationSaved(entity);

        PendingTaskEntity taskEntity = buildTaskEntity(100L);
        givenTaskReferenceExists(100L, taskEntity);

        PendingDailyRecommendation recommendation = buildDomainRecommendation(10L, LocalDate.now(), List.of(100L));

        PendingDailyRecommendation result = performUpsert(recommendation);

        thenRecommendationIsCreated(result, 1L, List.of(100L));
        thenTaskJoinsSaved();
    }

    @Test
    @DisplayName("Actualiza la decisión de respuesta (aceptada/rechazada) en una recomendación diaria")
    void updateDecisionShouldUpdateFields() {
        AppUserEntity user = buildUser(10L);
        PendingDailyRecommendationEntity entity = buildRecommendationEntity(1L, user, LocalDate.now(), RecommendationResponseDecision.PENDING);

        givenRecommendationByIdReturns(1L, entity);
        givenRecommendationSaved(entity);

        PendingDailyRecommendation result = performUpdateDecision(1L, RecommendationResponseDecision.ACCEPTED, Instant.now());

        thenDecisionIs(result, RecommendationResponseDecision.ACCEPTED);
    }

    @Test
    @DisplayName("Lanza excepción si la recomendación a la cual actualizar la decisión no existe")
    void updateDecisionShouldThrowNotFoundWhenDoesNotExist() {
        givenRecommendationByIdReturnsEmpty(1L);

        assertThatThrownBy(() -> performUpdateDecision(1L, RecommendationResponseDecision.ACCEPTED, Instant.now()))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    @DisplayName("Devuelve un conjunto vacío de IDs aceptados si no existe recomendación hoy")
    void findAcceptedTaskIdsShouldReturnEmptyWhenRecommendationNotFound() {
        givenRecommendationByDateReturnsEmpty(10L, LocalDate.now());

        Set<Long> taskIds = performFindAcceptedTaskIds(10L, LocalDate.now());

        thenTaskIdsIsEmpty(taskIds);
    }

    @Test
    @DisplayName("Devuelve el conjunto de IDs recomendados si la recomendación del día fue aceptada")
    void findAcceptedTaskIdsShouldReturnTaskIdsWhenAccepted() {
        AppUserEntity user = buildUser(10L);
        PendingDailyRecommendationEntity entity = buildRecommendationEntity(1L, user, LocalDate.now(), RecommendationResponseDecision.ACCEPTED);

        givenRecommendationByDateReturns(10L, LocalDate.now(), entity);

        PendingTaskEntity task = buildTaskEntity(100L);
        PendingDailyRecommendationTaskEntity join = buildJoinEntity(entity, task);
        givenTaskJoinsReturns(1L, List.of(join));

        Set<Long> taskIds = performFindAcceptedTaskIds(10L, LocalDate.now());

        thenTaskIdsContains(taskIds, 100L);
    }

    // --- arrange ---

    private void givenRecommendationByDateReturns(Long userId, LocalDate date, PendingDailyRecommendationEntity entity) {
        when(jpaRepository.findByUser_IdAndRecommendationDate(userId, date)).thenReturn(Optional.of(entity));
    }

    private void givenRecommendationByDateReturnsEmpty(Long userId, LocalDate date) {
        when(jpaRepository.findByUser_IdAndRecommendationDate(userId, date)).thenReturn(Optional.empty());
    }

    private void givenRecommendationByIdAndUserReturns(Long id, Long userId, PendingDailyRecommendationEntity entity) {
        when(jpaRepository.findByIdAndUser_Id(id, userId)).thenReturn(Optional.of(entity));
    }

    private void givenRecommendationByIdReturns(Long id, PendingDailyRecommendationEntity entity) {
        when(jpaRepository.findById(id)).thenReturn(Optional.of(entity));
    }

    private void givenRecommendationByIdReturnsEmpty(Long id) {
        when(jpaRepository.findById(id)).thenReturn(Optional.empty());
    }

    private void givenTaskJoinsReturns(Long recId, List<PendingDailyRecommendationTaskEntity> joins) {
        when(taskJoinJpaRepository.findAllByRecommendation_Id(recId)).thenReturn(joins);
    }

    private void givenUserExists(Long userId, AppUserEntity user) {
        when(appUserRepository.getReferenceById(userId)).thenReturn(user);
    }

    private void givenRecommendationSaved(PendingDailyRecommendationEntity entity) {
        when(jpaRepository.save(any(PendingDailyRecommendationEntity.class))).thenReturn(entity);
    }

    private void givenTaskReferenceExists(Long id, PendingTaskEntity task) {
        when(taskJpaRepository.getReferenceById(id)).thenReturn(task);
    }

    // --- act ---

    private Optional<PendingDailyRecommendation> performFindByUserIdAndDate(Long userId, LocalDate date) {
        return repository.findByUserIdAndDate(userId, date);
    }

    private Optional<PendingDailyRecommendation> performFindByIdAndUserId(Long id, Long userId) {
        return repository.findByIdAndUserId(id, userId);
    }

    private PendingDailyRecommendation performUpsert(PendingDailyRecommendation rec) {
        return repository.upsert(rec);
    }

    private PendingDailyRecommendation performUpdateDecision(Long id, RecommendationResponseDecision decision, Instant decidedAt) {
        return repository.updateDecision(id, decision, decidedAt);
    }

    private Set<Long> performFindAcceptedTaskIds(Long userId, LocalDate date) {
        return repository.findAcceptedTaskIds(userId, date);
    }

    // --- assert ---

    private void thenResultContainsId(Optional<PendingDailyRecommendation> result, Long expectedId) {
        assertThat(result).isPresent();
        assertThat(result.get().getId()).isEqualTo(expectedId);
    }

    private void thenRecommendationIsCreated(PendingDailyRecommendation result, Long expectedId, List<Long> expectedTaskIds) {
        assertThat(result.getId()).isEqualTo(expectedId);
        assertThat(result.getRecommendedTaskIds()).containsExactlyElementsOf(expectedTaskIds);
    }

    private void thenTaskJoinsSaved() {
        verify(taskJoinJpaRepository).saveAll(anyList());
    }

    private void thenDecisionIs(PendingDailyRecommendation result, RecommendationResponseDecision expected) {
        assertThat(result.getDecision()).isEqualTo(expected);
    }

    private void thenTaskIdsIsEmpty(Set<Long> taskIds) {
        assertThat(taskIds).isEmpty();
    }

    private void thenTaskIdsContains(Set<Long> taskIds, Long id) {
        assertThat(taskIds).containsExactly(id);
    }

    // --- helpers ---

    private AppUserEntity buildUser(Long id) {
        AppUserEntity user = new AppUserEntity();
        user.setId(id);
        return user;
    }

    private PendingDailyRecommendationEntity buildRecommendationEntity(Long id, AppUserEntity user, LocalDate date, RecommendationResponseDecision decision) {
        return PendingDailyRecommendationEntity.builder()
                .id(id)
                .user(user)
                .recommendationDate(date)
                .decision(decision)
                .build();
    }

    private PendingTaskEntity buildTaskEntity(Long id) {
        return PendingTaskEntity.builder().id(id).build();
    }

    private PendingDailyRecommendation buildDomainRecommendation(Long userId, LocalDate date, List<Long> taskIds) {
        return PendingDailyRecommendation.builder()
                .userId(userId)
                .recommendationDate(date)
                .recommendedTaskIds(taskIds)
                .build();
    }

    private PendingDailyRecommendationTaskEntity buildJoinEntity(PendingDailyRecommendationEntity rec, PendingTaskEntity task) {
        return PendingDailyRecommendationTaskEntity.builder()
                .recommendation(rec)
                .task(task)
                .build();
    }
}
