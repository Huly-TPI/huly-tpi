package com.huly.backend.domain.useCase.pendingRecommendation;

import com.huly.backend.domain.dto.pendingRecommendation.PendingRecommendationResponse;
import com.huly.backend.domain.dto.pendingRecommendation.RespondToRecommendationRequest;
import com.huly.backend.domain.exception.ResourceNotFoundException;
import com.huly.backend.domain.mapper.pendingRecommendation.PendingRecommendationMapper;
import com.huly.backend.domain.model.enums.RecommendationResponseDecision;
import com.huly.backend.domain.model.pending.PendingDailyRecommendation;
import com.huly.backend.domain.repository.pending.PendingRecommendationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RespondToRecommendationUseCaseTest {

    @Mock
    private PendingRecommendationRepository pendingRecommendationRepository;

    private RespondToRecommendationUseCase useCase;

    @BeforeEach
    void setUp() {
        useCase = new RespondToRecommendationUseCase(
                pendingRecommendationRepository,
                new PendingRecommendationMapper()
        );
    }

    @Test
    @DisplayName("Acepta la recomendación diaria de forma exitosa")
    void executeShouldAcceptRecommendation() {
        PendingDailyRecommendation recommendation = recommendation(RecommendationResponseDecision.PENDING);
        PendingDailyRecommendation accepted = recommendation(RecommendationResponseDecision.ACCEPTED);

        givenRecommendationExists(1L, 10L, recommendation);
        givenDecisionUpdated(1L, RecommendationResponseDecision.ACCEPTED, accepted);

        PendingRecommendationResponse response = executeUseCase(1L, 10L, RecommendationResponseDecision.ACCEPTED);

        thenDecisionIs(response, RecommendationResponseDecision.ACCEPTED);
        thenUpdateDecisionCalled(1L, RecommendationResponseDecision.ACCEPTED);
    }

    @Test
    @DisplayName("Rechaza la recomendación diaria de forma exitosa")
    void executeShouldRejectRecommendation() {
        PendingDailyRecommendation recommendation = recommendation(RecommendationResponseDecision.PENDING);
        PendingDailyRecommendation rejected = recommendation(RecommendationResponseDecision.REJECTED);

        givenRecommendationExists(1L, 10L, recommendation);
        givenDecisionUpdated(1L, RecommendationResponseDecision.REJECTED, rejected);

        PendingRecommendationResponse response = executeUseCase(1L, 10L, RecommendationResponseDecision.REJECTED);

        thenDecisionIs(response, RecommendationResponseDecision.REJECTED);
        thenUpdateDecisionCalled(1L, RecommendationResponseDecision.REJECTED);
    }

    @Test
    @DisplayName("Lanza excepción cuando la recomendación a la cual responder no existe")
    void executeShouldThrowNotFoundWhenRecommendationDoesNotExist() {
        givenRecommendationDoesNotExist(1L, 10L);

        assertThatThrownBy(() -> executeUseCase(1L, 10L, RecommendationResponseDecision.ACCEPTED))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    // --- arrange ---

    private void givenRecommendationExists(Long id, Long userId, PendingDailyRecommendation rec) {
        when(pendingRecommendationRepository.findByIdAndUserId(id, userId)).thenReturn(Optional.of(rec));
    }

    private void givenRecommendationDoesNotExist(Long id, Long userId) {
        when(pendingRecommendationRepository.findByIdAndUserId(id, userId)).thenReturn(Optional.empty());
    }

    private void givenDecisionUpdated(Long id, RecommendationResponseDecision decision, PendingDailyRecommendation result) {
        when(pendingRecommendationRepository.updateDecision(eq(id), eq(decision), any(Instant.class)))
                .thenReturn(result);
    }

    // --- act ---

    private PendingRecommendationResponse executeUseCase(Long id, Long userId, RecommendationResponseDecision decision) {
        return useCase.execute(new RespondToRecommendationRequest(id, userId, decision));
    }

    // --- assert ---

    private void thenDecisionIs(PendingRecommendationResponse response, RecommendationResponseDecision decision) {
        assertThat(response.recommendationId()).isEqualTo(1L);
        assertThat(response.decision()).isEqualTo(decision);
    }

    private void thenUpdateDecisionCalled(Long id, RecommendationResponseDecision decision) {
        verify(pendingRecommendationRepository).updateDecision(eq(id), eq(decision), any());
    }

    // --- helpers ---

    private PendingDailyRecommendation recommendation(RecommendationResponseDecision decision) {
        return PendingDailyRecommendation.builder()
                .id(1L)
                .userId(10L)
                .recommendationDate(LocalDate.now())
                .decision(decision)
                .recommendedTaskIds(List.of(100L))
                .decidedAt(decision != RecommendationResponseDecision.PENDING ? Instant.now() : null)
                .build();
    }
}
