package com.huly.backend.domain.service.user;

import com.huly.backend.domain.repository.user.UserDetailDomainRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserActivityServiceTest {

    private static final Long USER_ID = 10L;
    private static final LocalDate TODAY = LocalDate.of(2026, 6, 12);

    @Mock
    private UserDetailDomainRepository userDetailDomainRepository;

    @InjectMocks
    private UserActivityService service;

    @Test
    @DisplayName("Avanza la última fecha de acceso cuando no hay comeback pendiente")
    void registerActivityShouldAdvanceLastLoginWhenNoComebackPending() {
        givenLastLoginDaysAgo(3);

        registerActivity();

        thenLastLoginUpdated();
    }

    @Test
    @DisplayName("Avanza la última fecha de acceso cuando nunca fue visto")
    void registerActivityShouldAdvanceLastLoginWhenNeverSeen() {
        givenNeverSeen();

        registerActivity();

        thenLastLoginUpdated();
    }

    @Test
    @DisplayName("No avanza la última fecha de acceso cuando hay un comeback pendiente")
    void registerActivityShouldNotAdvanceLastLoginWhenComebackPending() {
        // Brecha >= umbral (10 días): hay un comeback pendiente, no hay que borrar la brecha.
        givenLastLoginDaysAgo(15);

        registerActivity();

        thenLastLoginNotUpdated();
    }

    // --- arrange ---
    private void givenLastLoginDaysAgo(int days) {
        when(userDetailDomainRepository.findLastLoginDate(USER_ID)).thenReturn(Optional.of(TODAY.minusDays(days)));
    }

    private void givenNeverSeen() {
        when(userDetailDomainRepository.findLastLoginDate(USER_ID)).thenReturn(Optional.empty());
    }

    // --- act ---
    private void registerActivity() {
        service.registerActivity(USER_ID, TODAY);
    }

    // --- assert ---
    private void thenLastLoginUpdated() {
        verify(userDetailDomainRepository).updateLastLoginDate(USER_ID, TODAY);
    }

    private void thenLastLoginNotUpdated() {
        verify(userDetailDomainRepository, never()).updateLastLoginDate(anyLong(), any());
    }
}
