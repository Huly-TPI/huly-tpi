package com.huly.backend.infrastructure.repository.jpaRepository.specification;

import com.huly.backend.domain.model.enums.RiskSeverity;
import com.huly.backend.infrastructure.repository.entity.RiskWordEntity;
import jakarta.persistence.criteria.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.jpa.domain.Specification;

import java.lang.reflect.Constructor;
import java.lang.reflect.Modifier;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@SuppressWarnings({"unchecked", "rawtypes"})
@ExtendWith(MockitoExtension.class)
class RiskWordFilterSpecTest {

    @Mock private Root<RiskWordEntity> root;
    @Mock private CriteriaQuery<?> query;
    @Mock private CriteriaBuilder builder;
    @Mock private Path wordPath;
    @Mock private Path activePath;
    @Mock private Path severityPath;
    @Mock private Expression<String> lowerExpr;
    @Mock private Predicate wordPredicate;
    @Mock private Predicate activePredicate;
    @Mock private Predicate severityPredicate;
    @Mock private Predicate conjunction;
    @Mock private Predicate combined;

    @BeforeEach
    void setUp() {
        when(builder.conjunction()).thenReturn(conjunction);
    }

    @Test
    void constructor_shouldBePrivate() throws Exception {
        Constructor<RiskWordFilterSpec> ctor = RiskWordFilterSpec.class.getDeclaredConstructor();
        assertThat(Modifier.isPrivate(ctor.getModifiers())).isTrue();
        ctor.setAccessible(true);
        ctor.newInstance();
    }

    @Test
    void build_shouldReturnConjunctionOnly_whenAllFiltersAreNull() {
        Specification<RiskWordEntity> spec = RiskWordFilterSpec.build(null, null, null);

        Predicate result = spec.toPredicate(root, query, builder);

        assertThat(result).isEqualTo(conjunction);
        verify(builder, never()).like(any(), anyString());
        verify(builder, never()).equal(any(), any());
    }

    @Test
    void build_shouldSkipWordFilter_whenWordIsBlank() {
        Specification<RiskWordEntity> spec = RiskWordFilterSpec.build("   ", null, null);

        spec.toPredicate(root, query, builder);

        verify(builder, never()).like(any(), anyString());
    }

    @Test
    void build_shouldSkipSeverityFilter_whenSeverityIsBlank() {
        Specification<RiskWordEntity> spec = RiskWordFilterSpec.build(null, null, "  ");

        spec.toPredicate(root, query, builder);

        verify(builder, never()).equal(any(), any(RiskSeverity.class));
    }

    @Test
    void build_shouldApplyWordFilter_whenWordIsNotBlank() {
        when(root.get("word")).thenReturn(wordPath);
        when(builder.lower(wordPath)).thenReturn(lowerExpr);
        when(builder.like(lowerExpr, "%ansiedad%")).thenReturn(wordPredicate);
        when(builder.and(any(Predicate[].class))).thenReturn(combined);

        Specification<RiskWordEntity> spec = RiskWordFilterSpec.build("ansiedad", null, null);
        Predicate result = spec.toPredicate(root, query, builder);

        assertThat(result).isEqualTo(combined);
        verify(builder).like(lowerExpr, "%ansiedad%");
    }

    @Test
    void build_shouldApplyWordFilter_caseInsensitive() {
        when(root.get("word")).thenReturn(wordPath);
        when(builder.lower(wordPath)).thenReturn(lowerExpr);
        when(builder.like(lowerExpr, "%ansie%")).thenReturn(wordPredicate);
        when(builder.and(any(Predicate[].class))).thenReturn(combined);

        Specification<RiskWordEntity> spec = RiskWordFilterSpec.build("ANSIE", null, null);
        spec.toPredicate(root, query, builder);

        verify(builder).like(lowerExpr, "%ansie%");
    }

    @Test
    void build_shouldApplyActiveFilter_whenActiveIsTrue() {
        when(root.get("active")).thenReturn(activePath);
        when(builder.equal(activePath, true)).thenReturn(activePredicate);
        when(builder.and(any(Predicate[].class))).thenReturn(combined);

        Specification<RiskWordEntity> spec = RiskWordFilterSpec.build(null, true, null);
        Predicate result = spec.toPredicate(root, query, builder);

        assertThat(result).isEqualTo(combined);
        verify(builder).equal(activePath, true);
    }

    @Test
    void build_shouldApplyActiveFilter_whenActiveIsFalse() {
        when(root.get("active")).thenReturn(activePath);
        when(builder.equal(activePath, false)).thenReturn(activePredicate);
        when(builder.and(any(Predicate[].class))).thenReturn(combined);

        Specification<RiskWordEntity> spec = RiskWordFilterSpec.build(null, false, null);
        spec.toPredicate(root, query, builder);

        verify(builder).equal(activePath, false);
    }

    @Test
    void build_shouldApplySeverityFilter_whenSeverityIsNotBlank() {
        when(root.get("severity")).thenReturn(severityPath);
        when(builder.equal(severityPath, RiskSeverity.HIGH)).thenReturn(severityPredicate);
        when(builder.and(any(Predicate[].class))).thenReturn(combined);

        Specification<RiskWordEntity> spec = RiskWordFilterSpec.build(null, null, "HIGH");
        Predicate result = spec.toPredicate(root, query, builder);

        assertThat(result).isEqualTo(combined);
        verify(builder).equal(severityPath, RiskSeverity.HIGH);
    }

    @Test
    void build_shouldApplySeverityFilter_caseInsensitive() {
        when(root.get("severity")).thenReturn(severityPath);
        when(builder.equal(severityPath, RiskSeverity.LOW)).thenReturn(severityPredicate);
        when(builder.and(any(Predicate[].class))).thenReturn(combined);

        Specification<RiskWordEntity> spec = RiskWordFilterSpec.build(null, null, "low");
        spec.toPredicate(root, query, builder);

        verify(builder).equal(severityPath, RiskSeverity.LOW);
    }

    @Test
    void build_shouldApplyAllFilters_whenAllAreProvided() {
        when(root.get("word")).thenReturn(wordPath);
        when(root.get("active")).thenReturn(activePath);
        when(root.get("severity")).thenReturn(severityPath);
        when(builder.lower(wordPath)).thenReturn(lowerExpr);
        when(builder.like(lowerExpr, "%suicidio%")).thenReturn(wordPredicate);
        when(builder.equal(activePath, true)).thenReturn(activePredicate);
        when(builder.equal(severityPath, RiskSeverity.HIGH)).thenReturn(severityPredicate);
        when(builder.and(any(Predicate[].class))).thenReturn(combined);

        Specification<RiskWordEntity> spec = RiskWordFilterSpec.build("suicidio", true, "HIGH");
        spec.toPredicate(root, query, builder);

        verify(builder).like(lowerExpr, "%suicidio%");
        verify(builder).equal(activePath, true);
        verify(builder).equal(severityPath, RiskSeverity.HIGH);
    }
}
