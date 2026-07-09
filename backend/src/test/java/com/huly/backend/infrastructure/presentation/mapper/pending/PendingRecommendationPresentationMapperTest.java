package com.huly.backend.infrastructure.presentation.mapper.pending;

import com.huly.backend.domain.model.enums.RecommendationResponseDecision;
import com.huly.backend.infrastructure.presentation.dto.pending.PendingRecommendationResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class PendingRecommendationPresentationMapperTest {

    private PendingRecommendationPresentationMapper mapper;

    @BeforeEach
    void setUp() {
        mapper = new PendingRecommendationPresentationMapper();
    }

    @Test
    @DisplayName("Mapea DTO de respuesta de dominio de recomendación a DTO de presentación HTTP")
    void toResponseShouldMapCorrectly() {
        var domainResponse = buildDomainResponse(RecommendationResponseDecision.ACCEPTED, true);

        PendingRecommendationResponse dto = mapToResponse(domainResponse);

        thenResponseMatches(dto, 1L, "ACCEPTED", true, List.of(100L, 200L));
    }

    @Test
    @DisplayName("Mapea DTO de respuesta con decisión nula a DTO de presentación HTTP")
    void toResponseShouldMapNullDecisionCorrectly() {
        var domainResponse = buildDomainResponse(null, false);

        PendingRecommendationResponse dto = mapToResponse(domainResponse);

        thenDecisionIsNull(dto);
        thenIsNewIs(dto, false);
    }

    // --- act ---

    private PendingRecommendationResponse mapToResponse(com.huly.backend.domain.dto.pendingRecommendation.PendingRecommendationResponse domainResponse) {
        return mapper.toResponse(domainResponse);
    }

    // --- assert ---

    private void thenResponseMatches(PendingRecommendationResponse dto, Long recId, String decision, boolean isNew, List<Long> taskIds) {
        assertThat(dto.recommendationId()).isEqualTo(recId);
        assertThat(dto.decision()).isEqualTo(decision);
        assertThat(dto.recommendedTaskIds()).containsExactlyElementsOf(taskIds);
        assertThat(dto.isNew()).isEqualTo(isNew);
    }

    private void thenDecisionIsNull(PendingRecommendationResponse dto) {
        assertThat(dto.decision()).isNull();
    }

    private void thenIsNewIs(PendingRecommendationResponse dto, boolean expected) {
        assertThat(dto.isNew()).isEqualTo(expected);
    }

    // --- helpers ---

    private com.huly.backend.domain.dto.pendingRecommendation.PendingRecommendationResponse buildDomainResponse(RecommendationResponseDecision decision, boolean isNew) {
        return new com.huly.backend.domain.dto.pendingRecommendation.PendingRecommendationResponse(
                1L, LocalDate.now(), decision, List.of(100L, 200L), isNew, true
        );
    }
}
