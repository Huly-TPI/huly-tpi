package com.huly.backend.presentation.controller;
import com.huly.backend.domain.model.Badge;
import com.huly.backend.domain.model.UserBadge;
import com.huly.backend.domain.useCase.badge.GetAllBadgesUseCase;
import com.huly.backend.domain.useCase.badge.GetUserBadgesUseCase;
import com.huly.backend.infrastructure.presentation.controller.BadgeController;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BadgeControllerTest {

    @InjectMocks
    private BadgeController badgeController;
    @Mock
    private GetAllBadgesUseCase getAllBadgesUseCase;
    @Mock
    private GetUserBadgesUseCase getUserBadgesUseCase;
    @Mock
    private UserDetails userDetails;

    @Test 
    void getAllBadges_shouldReturnMappedBadgeList() {
        Badge badge = Badge.builder() 
                .id(1L)
                .code("PRIMER PASO")
                .name("Primer paso")
                .description("Empezaste tu camino.")
                .imageUrl("badge_primer_paso.webp")
                .build();
        when(getAllBadgesUseCase.execute()).thenReturn(List.of(badge));
        var response = badgeController.getAllBadges();
        assertThat(response.getStatusCode().is2xxSuccessful()).isTrue();
        assertThat(response.getBody()).hasSize(1);
        assertThat(response.getBody().get(0).code()).isEqualTo("PRIMER PASO");
    }

    @Test
    void getAllBadges_shouldReturnEmptyList_whenNoBadgesExist() {
        when(getAllBadgesUseCase.execute()).thenReturn(List.of());
        var response = badgeController.getAllBadges();
        assertThat(response.getStatusCode().is2xxSuccessful()).isTrue();
        assertThat(response.getBody()).isEmpty();
    }

    @Test 
    void getMyBadges_shouldReturnUserBadges() {
        Badge badge = Badge.builder() 
                .id(1L)
                .code("PRIMER PASO")
                .name("Primer paso")
                .build();
        UserBadge userBadge = UserBadge.builder()
                .id(1L)
                .userId(1L)
                .badge(badge)
                .obtainedAt(Instant.now())
                .build();
        when(userDetails.getUsername()).thenReturn("1");
        when(getUserBadgesUseCase.execute(1L)).thenReturn(List.of(userBadge));
        var response = badgeController.getMyBadges(userDetails);
        assertThat(response.getStatusCode().is2xxSuccessful()).isTrue();
        assertThat(response.getBody()).hasSize(1);
        assertThat(response.getBody().get(0).badge().code()).isEqualTo("PRIMER PASO");
    }

    @Test
    void getMyBadges_shouldReturnEmptyList_whenUserHasNoBadges() {
       when(userDetails.getUsername()).thenReturn("1");
       when(getUserBadgesUseCase.execute(1L)).thenReturn(List.of());
        var response = badgeController.getMyBadges(userDetails);
        assertThat(response.getStatusCode().is2xxSuccessful()).isTrue();
        assertThat(response.getBody()).isEmpty();
    }   
    
}
