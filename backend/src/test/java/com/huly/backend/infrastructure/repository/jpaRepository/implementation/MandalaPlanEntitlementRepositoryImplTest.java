package com.huly.backend.infrastructure.repository.jpaRepository.implementation;

import com.huly.backend.infrastructure.repository.entity.MandalaPlanEntitlementEntity;
import com.huly.backend.infrastructure.repository.jpaRepository.interfaces.IMandalaPlanEntitlementJpaRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MandalaPlanEntitlementRepositoryImplTest {

    @Mock
    private IMandalaPlanEntitlementJpaRepository jpaRepository;

    @InjectMocks
    private MandalaPlanEntitlementRepositoryImpl repository;

    @Test
    @DisplayName("Devuelve los ids de mandala habilitados para el plan")
    void findMandalaIdsByPlanCodeShouldMapEntityIds() {
        givenEntitlements("PREMIUM", entitlement("m1"), entitlement("m2"));

        List<String> result = findMandalaIds("PREMIUM");

        thenIdsAre(result, "m1", "m2");
    }

    @Test
    @DisplayName("Devuelve lista vacía cuando el plan no habilita mandalas")
    void findMandalaIdsByPlanCodeShouldReturnEmptyWhenNone() {
        givenEntitlements("FREE");

        List<String> result = findMandalaIds("FREE");

        thenEmpty(result);
    }

    // --- arrange ---
    private void givenEntitlements(String planCode, MandalaPlanEntitlementEntity... entities) {
        when(jpaRepository.findAllByPlanCode(planCode)).thenReturn(List.of(entities));
    }

    private MandalaPlanEntitlementEntity entitlement(String mandalaId) {
        return MandalaPlanEntitlementEntity.builder()
                .id(1L)
                .planCode("PREMIUM")
                .mandalaId(mandalaId)
                .build();
    }

    // --- act ---
    private List<String> findMandalaIds(String planCode) {
        return repository.findMandalaIdsByPlanCode(planCode);
    }

    // --- assert ---
    private void thenIdsAre(List<String> result, String... ids) {
        assertThat(result).containsExactly(ids);
    }

    private void thenEmpty(List<String> result) {
        assertThat(result).isEmpty();
    }
}
