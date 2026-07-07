package com.huly.backend.domain.useCase.badge;

import com.huly.backend.domain.dto.badge.GetAllBadgesResponse;
import com.huly.backend.domain.mapper.badge.GetAllBadgesMapper;
import com.huly.backend.domain.model.badge.Badge;
import com.huly.backend.domain.repository.badge.BadgeRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GetAllBadgesUseCaseTest {

    private static final String PRIMER_PASO = "PRIMER_PASO";
    private static final String VALENTIA = "VALENTÍA";

    @Mock
    private BadgeRepository badgeRepository;

    private GetAllBadgesUseCase getAllBadgesUseCase;

    @BeforeEach
    void setUp() {
        getAllBadgesUseCase = new GetAllBadgesUseCase(badgeRepository, new GetAllBadgesMapper());
    }

    @Test
    @DisplayName("Devuelve todas las insignias disponibles")
    void executeShouldReturnAllBadges() {
        givenConfiguredBadges();

        GetAllBadgesResponse result = getAllBadges();

        thenAllBadgesReturned(result);
    }

    @Test
    @DisplayName("Devuelve una lista vacía cuando no hay insignias configuradas")
    void executeShouldReturnEmptyListWhenNoBadgesExist() {
        givenNoConfiguredBadges();

        GetAllBadgesResponse result = getAllBadges();

        thenNoBadgesReturned(result);
    }

    // --- arrange ---

    private void givenConfiguredBadges() {
        when(badgeRepository.findAll()).thenReturn(List.of(
                Badge.builder().id(1L).code(PRIMER_PASO).name("Primer paso").build(),
                Badge.builder().id(2L).code(VALENTIA).name("Valentía").build()));
    }

    private void givenNoConfiguredBadges() {
        when(badgeRepository.findAll()).thenReturn(List.of());
    }

    // --- act ---

    private GetAllBadgesResponse getAllBadges() {
        return getAllBadgesUseCase.execute();
    }

    // --- assert ---

    private void thenAllBadgesReturned(GetAllBadgesResponse result) {
        assertThat(result.badges()).hasSize(2);
        assertThat(result.badges().get(0).code()).isEqualTo(PRIMER_PASO);
        assertThat(result.badges().get(1).code()).isEqualTo(VALENTIA);
        verify(badgeRepository).findAll();
    }

    private void thenNoBadgesReturned(GetAllBadgesResponse result) {
        assertThat(result.badges()).isEmpty();
        verify(badgeRepository).findAll();
    }
}
