package com.huly.backend.infrastructure.repository.jpaRepository.implementation;

import com.huly.backend.domain.model.emotionalRecommendation.EmotionalEvent;
import com.huly.backend.domain.model.enums.EmotionalEventSource;
import com.huly.backend.domain.model.enums.RecommendationDecision;
import com.huly.backend.infrastructure.repository.entity.ActivityEntity;
import com.huly.backend.infrastructure.repository.entity.AppUserEntity;
import com.huly.backend.infrastructure.repository.entity.EmotionalEventEntity;
import com.huly.backend.infrastructure.repository.jpaRepository.interfaces.AppUserRepository;
import com.huly.backend.infrastructure.repository.jpaRepository.interfaces.IActivityJpaRepository;
import com.huly.backend.infrastructure.repository.jpaRepository.interfaces.IEmotionalEventJpaRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Pageable;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EmotionalEventRepositoryImplTest {

    private static final Long USER_ID = 1L;
    private static final Long ACTIVITY_ID = 2L;
    private static final Long EVENT_ID = 10L;
    private static final Long MISSING_ID = 99L;
    private static final int LIMIT = 20;
    private static final int FEEDBACK_SCORE = 4;
    private static final Instant START = Instant.parse("2026-03-03T00:00:00Z");
    private static final List<Long> USER_IDS = List.of(1L, 2L);
    private static final List<Long> EMPTY_IDS = List.of();

    @Mock
    private IEmotionalEventJpaRepository emotionalEventJpaRepository;
    @Mock
    private AppUserRepository appUserRepository;
    @Mock
    private IActivityJpaRepository activityJpaRepository;

    @InjectMocks
    private EmotionalEventRepositoryImpl repository;

    @Test
    @DisplayName("Mapea el dominio a entidad al guardar y persiste usuario y actividad referenciados")
    void saveShouldPersistReferencedUserAndActivity() {
        givenReferencedUser();
        givenReferencedActivity();
        givenSaved(entity());

        save(domain());

        thenPersistedEntityHasUserAndActivity();
    }

    @Test
    @DisplayName("Mapea la entidad guardada a dominio al guardar")
    void saveShouldMapPersistedEntityToDomain() {
        givenReferencedUser();
        givenReferencedActivity();
        givenSaved(entity());

        EmotionalEvent result = save(domain());

        thenEventMatches(result, EVENT_ID, RecommendationDecision.ACCEPTED);
    }

    @Test
    @DisplayName("Conserva los ids nulos al guardar")
    void saveShouldMapNullIdsToNull() {
        givenSaved(entityWithNullIds());

        EmotionalEvent result = save(domainWithNullIds());

        thenNullIdsMappedToNull(result);
    }

    @Test
    @DisplayName("Devuelve el evento por id mapeado a dominio cuando existe")
    void findByIdShouldReturnMappedDomainWhenFound() {
        givenFoundById(EVENT_ID, entity());

        Optional<EmotionalEvent> result = findById(EVENT_ID);

        thenEventPresent(result, USER_ID, ACTIVITY_ID);
    }

    @Test
    @DisplayName("Devuelve vacío al buscar por id cuando no existe")
    void findByIdShouldReturnEmptyWhenMissing() {
        givenFoundById(MISSING_ID, null);

        Optional<EmotionalEvent> result = findById(MISSING_ID);

        thenEmpty(result);
    }

    @Test
    @DisplayName("Devuelve el historial reciente de recomendaciones mapeado y paginado")
    void findRecentRecommendationHistoryByUserIdShouldReturnMappedRecentEvents() {
        givenRecentHistory(List.of(entity()));

        List<EmotionalEvent> result = findRecentHistory(USER_ID, LIMIT);

        thenRequestedFirstPageOf(LIMIT);
        thenRecentEventMapped(result);
    }

    @Test
    @DisplayName("Devuelve vacío en el historial reciente cuando falta el usuario")
    void findRecentRecommendationHistoryByUserIdShouldReturnEmptyWhenUserIdIsMissing() {
        List<EmotionalEvent> result = findRecentHistory(null, LIMIT);

        thenEmpty(result);
    }

    @Test
    @DisplayName("Devuelve vacío en el historial reciente cuando el límite es cero o negativo")
    void findRecentRecommendationHistoryByUserIdShouldReturnEmptyWhenLimitIsZeroOrLess() {
        List<EmotionalEvent> zero = findRecentHistory(USER_ID, 0);
        List<EmotionalEvent> negative = findRecentHistory(USER_ID, -5);

        thenEmpty(zero);
        thenEmpty(negative);
    }

    @Test
    @DisplayName("Mapea los eventos del usuario ordenados por fecha descendente")
    void findByUserIdShouldReturnMappedList() {
        givenEventsByUserId(List.of(entity()));

        List<EmotionalEvent> result = findByUserId(USER_ID);

        thenSingleEventMappedById(result);
        thenQueriedByUserIdOrderedDesc();
    }

    @Test
    @DisplayName("Devuelve vacío al buscar por usuario cuando el id es nulo")
    void findByUserIdShouldReturnEmptyWhenUserIdIsNull() {
        List<EmotionalEvent> result = findByUserId(null);

        thenEmpty(result);
    }

    @Test
    @DisplayName("Mapea los eventos de recomendación del usuario")
    void findRecommendationEventsByUserIdShouldReturnMappedList() {
        givenRecommendationEventsByUserId(List.of(entity()));

        List<EmotionalEvent> result = findRecommendationEventsByUserId(USER_ID);

        thenSingleRecommendationEventMapped(result);
        thenQueriedRecommendationEventsByUserId();
    }

    @Test
    @DisplayName("Devuelve vacío en eventos de recomendación cuando el usuario es nulo")
    void findRecommendationEventsByUserIdShouldReturnEmptyWhenUserIdIsNull() {
        List<EmotionalEvent> result = findRecommendationEventsByUserId(null);

        thenEmpty(result);
    }

    @Test
    @DisplayName("Mapea todos los eventos con decisión de recomendación")
    void findAllRecommendationEventsShouldReturnMappedList() {
        givenAllRecommendationEvents(List.of(entity()));

        List<EmotionalEvent> result = findAllRecommendationEvents();

        thenSingleEventMappedById(result);
    }

    @Test
    @DisplayName("Delega en todos los eventos de recomendación cuando el inicio es nulo")
    void findAllRecommendationEventsAfterShouldDelegateWhenStartIsNull() {
        givenAllRecommendationEvents(List.of(entity()));

        List<EmotionalEvent> result = findAllRecommendationEventsAfter(null);

        thenSingleEventMappedById(result);
    }

    @Test
    @DisplayName("Mapea los eventos de recomendación posteriores al inicio dado")
    void findAllRecommendationEventsAfterShouldReturnMappedListWhenStartIsPresent() {
        givenRecommendationEventsAfter(START, List.of(entity()));

        List<EmotionalEvent> result = findAllRecommendationEventsAfter(START);

        thenSingleEventMappedById(result);
    }

    @Test
    @DisplayName("Devuelve vacío al buscar por lista de usuarios nula")
    void findByUserIdsShouldReturnEmptyWhenNull() {
        List<EmotionalEvent> result = findByUserIds(null);

        thenEmpty(result);
    }

    @Test
    @DisplayName("Devuelve vacío al buscar por lista de usuarios vacía")
    void findByUserIdsShouldReturnEmptyWhenEmpty() {
        List<EmotionalEvent> result = findByUserIds(EMPTY_IDS);

        thenEmpty(result);
    }

    @Test
    @DisplayName("Mapea los eventos de la lista de usuarios ordenados por fecha ascendente")
    void findByUserIdsShouldReturnMappedListWhenIdsPresent() {
        givenEventsByUserIds(USER_IDS, List.of(entity()));

        List<EmotionalEvent> result = findByUserIds(USER_IDS);

        thenSingleEventMappedById(result);
    }

    // --- arrange ---
    private void givenReferencedUser() {
        when(appUserRepository.getReferenceById(USER_ID)).thenReturn(user(USER_ID));
    }

    private void givenReferencedActivity() {
        when(activityJpaRepository.getReferenceById(ACTIVITY_ID)).thenReturn(activity(ACTIVITY_ID));
    }

    private void givenSaved(EmotionalEventEntity entity) {
        when(emotionalEventJpaRepository.save(any())).thenReturn(entity);
    }

    private void givenFoundById(Long id, EmotionalEventEntity entity) {
        when(emotionalEventJpaRepository.findById(id)).thenReturn(Optional.ofNullable(entity));
    }

    private void givenRecentHistory(List<EmotionalEventEntity> entities) {
        when(emotionalEventJpaRepository.findRecommendationHistoryByUserId(eq(USER_ID), any(Pageable.class)))
                .thenReturn(entities);
    }

    private void givenEventsByUserId(List<EmotionalEventEntity> entities) {
        when(emotionalEventJpaRepository.findByUserIdOrderByCreatedAtDesc(USER_ID)).thenReturn(entities);
    }

    private void givenRecommendationEventsByUserId(List<EmotionalEventEntity> entities) {
        when(emotionalEventJpaRepository.findByUserIdAndRecommendationDecisionIsNotNullOrderByCreatedAtDesc(USER_ID))
                .thenReturn(entities);
    }

    private void givenAllRecommendationEvents(List<EmotionalEventEntity> entities) {
        when(emotionalEventJpaRepository.findByRecommendationDecisionIsNotNull()).thenReturn(entities);
    }

    private void givenRecommendationEventsAfter(Instant start, List<EmotionalEventEntity> entities) {
        when(emotionalEventJpaRepository.findByRecommendationDecisionIsNotNullAndCreatedAtAfter(start))
                .thenReturn(entities);
    }

    private void givenEventsByUserIds(List<Long> userIds, List<EmotionalEventEntity> entities) {
        when(emotionalEventJpaRepository.findByUserIdInOrderByCreatedAtAsc(userIds)).thenReturn(entities);
    }

    private EmotionalEvent domain() {
        Instant now = Instant.now();
        return EmotionalEvent.builder()
                .id(EVENT_ID)
                .userId(USER_ID)
                .source(EmotionalEventSource.CHATBOT)
                .inputText("texto")
                .detectedEmotion("ANSIEDAD")
                .confidence(0.9)
                .valence(-0.8)
                .arousal(0.9)
                .dominance(-0.7)
                .intensity(0.8)
                .userGoal("calmarme")
                .generatedRecommendation("Respira")
                .recommendedActivityId(ACTIVITY_ID)
                .recommendationDecision(RecommendationDecision.ACCEPTED)
                .chosenActivityId(ACTIVITY_ID)
                .feedbackScore(FEEDBACK_SCORE)
                .feedbackText("mejor")
                .createdAt(now)
                .updatedAt(now)
                .build();
    }

    private EmotionalEventEntity entity() {
        Instant now = Instant.now();
        return EmotionalEventEntity.builder()
                .id(EVENT_ID)
                .user(user(USER_ID))
                .source(EmotionalEventSource.CHATBOT)
                .inputText("texto")
                .detectedEmotion("ANSIEDAD")
                .confidence(0.9)
                .valence(-0.8)
                .arousal(0.9)
                .dominance(-0.7)
                .intensity(0.8)
                .userGoal("calmarme")
                .generatedRecommendation("Respira")
                .recommendedActivity(activity(ACTIVITY_ID))
                .chosenActivity(activity(ACTIVITY_ID))
                .recommendationDecision(RecommendationDecision.ACCEPTED)
                .feedbackScore(FEEDBACK_SCORE)
                .feedbackText("mejor")
                .createdAt(now)
                .updatedAt(now)
                .build();
    }

    private EmotionalEvent domainWithNullIds() {
        return EmotionalEvent.builder()
                .id(EVENT_ID)
                .userId(null)
                .source(EmotionalEventSource.CHATBOT)
                .recommendedActivityId(null)
                .chosenActivityId(null)
                .build();
    }

    private EmotionalEventEntity entityWithNullIds() {
        return EmotionalEventEntity.builder()
                .id(EVENT_ID)
                .user(null)
                .source(EmotionalEventSource.CHATBOT)
                .recommendedActivity(null)
                .chosenActivity(null)
                .build();
    }

    private AppUserEntity user(Long id) {
        AppUserEntity user = new AppUserEntity();
        user.setId(id);
        return user;
    }

    private ActivityEntity activity(Long id) {
        ActivityEntity activity = new ActivityEntity();
        activity.setId(id);
        return activity;
    }

    // --- act ---
    private EmotionalEvent save(EmotionalEvent event) {
        return repository.save(event);
    }

    private Optional<EmotionalEvent> findById(Long id) {
        return repository.findById(id);
    }

    private List<EmotionalEvent> findRecentHistory(Long userId, int limit) {
        return repository.findRecentRecommendationHistoryByUserId(userId, limit);
    }

    private List<EmotionalEvent> findByUserId(Long userId) {
        return repository.findByUserId(userId);
    }

    private List<EmotionalEvent> findRecommendationEventsByUserId(Long userId) {
        return repository.findRecommendationEventsByUserId(userId);
    }

    private List<EmotionalEvent> findAllRecommendationEvents() {
        return repository.findAllRecommendationEvents();
    }

    private List<EmotionalEvent> findAllRecommendationEventsAfter(Instant start) {
        return repository.findAllRecommendationEventsAfter(start);
    }

    private List<EmotionalEvent> findByUserIds(List<Long> userIds) {
        return repository.findByUserIds(userIds);
    }

    // --- assert ---
    private void thenPersistedEntityHasUserAndActivity() {
        ArgumentCaptor<EmotionalEventEntity> captor = ArgumentCaptor.forClass(EmotionalEventEntity.class);
        verify(emotionalEventJpaRepository).save(captor.capture());
        assertThat(captor.getValue().getUser().getId()).isEqualTo(USER_ID);
        assertThat(captor.getValue().getRecommendedActivity().getId()).isEqualTo(ACTIVITY_ID);
    }

    private void thenEventMatches(EmotionalEvent result, Long expectedId, RecommendationDecision decision) {
        assertThat(result.getId()).isEqualTo(expectedId);
        assertThat(result.getRecommendationDecision()).isEqualTo(decision);
    }

    private void thenNullIdsMappedToNull(EmotionalEvent result) {
        assertThat(result.getUserId()).isNull();
        assertThat(result.getRecommendedActivityId()).isNull();
        assertThat(result.getChosenActivityId()).isNull();
    }

    private void thenEventPresent(Optional<EmotionalEvent> result, Long userId, Long activityId) {
        assertThat(result).isPresent();
        assertThat(result.get().getUserId()).isEqualTo(userId);
        assertThat(result.get().getRecommendedActivityId()).isEqualTo(activityId);
    }

    private void thenRequestedFirstPageOf(int expectedSize) {
        ArgumentCaptor<Pageable> pageableCaptor = ArgumentCaptor.forClass(Pageable.class);
        verify(emotionalEventJpaRepository).findRecommendationHistoryByUserId(eq(USER_ID), pageableCaptor.capture());
        assertThat(pageableCaptor.getValue().getPageNumber()).isZero();
        assertThat(pageableCaptor.getValue().getPageSize()).isEqualTo(expectedSize);
    }

    private void thenRecentEventMapped(List<EmotionalEvent> result) {
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getUserId()).isEqualTo(USER_ID);
        assertThat(result.get(0).getRecommendedActivityId()).isEqualTo(ACTIVITY_ID);
        assertThat(result.get(0).getFeedbackScore()).isEqualTo(FEEDBACK_SCORE);
    }

    private void thenSingleEventMappedById(List<EmotionalEvent> result) {
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getId()).isEqualTo(EVENT_ID);
        assertThat(result.get(0).getUserId()).isEqualTo(USER_ID);
    }

    private void thenQueriedByUserIdOrderedDesc() {
        verify(emotionalEventJpaRepository).findByUserIdOrderByCreatedAtDesc(USER_ID);
    }

    private void thenSingleRecommendationEventMapped(List<EmotionalEvent> result) {
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getRecommendationDecision()).isEqualTo(RecommendationDecision.ACCEPTED);
    }

    private void thenQueriedRecommendationEventsByUserId() {
        verify(emotionalEventJpaRepository).findByUserIdAndRecommendationDecisionIsNotNullOrderByCreatedAtDesc(USER_ID);
    }

    private void thenEmpty(Optional<EmotionalEvent> result) {
        assertThat(result).isEmpty();
    }

    private void thenEmpty(List<EmotionalEvent> result) {
        assertThat(result).isEmpty();
    }
}
