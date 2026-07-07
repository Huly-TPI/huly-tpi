package com.huly.backend.domain.useCase.pushNotification;

import com.huly.backend.domain.model.enums.UserRole;
import com.huly.backend.domain.model.enums.UserStatus;
import com.huly.backend.domain.model.user.AppUser;
import com.huly.backend.domain.port.EmailPort;
import com.huly.backend.domain.repository.user.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReEngagementEmailSchedulerTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private EmailPort emailPort;

    @InjectMocks
    private ReEngagementEmailScheduler reEngagementEmailScheduler;

    private Instant cutoffLowerBound;
    private Instant cutoffUpperBound;

    @Test
    @DisplayName("Envía un email de re-enganche a cada usuario inactivo")
    void sendReEngagementEmailsShouldSendEmailToEachInactiveUser() {
        // --- arrange ---
        givenTwoInactiveUsers();
        // --- act ---
        sendReEngagementEmails();
        // --- assert ---
        thenReEngagementSentTo("user1@huly", "tok-1");
        thenReEngagementSentTo("user2@huly", "tok-2");
    }

    @Test
    @DisplayName("No hace nada cuando no hay usuarios inactivos")
    void sendReEngagementEmailsShouldDoNothingWhenNoInactiveUsers() {
        // --- arrange ---
        givenNoInactiveUsers();
        // --- act ---
        sendReEngagementEmails();
        // --- assert ---
        thenNoReEngagementSent();
    }

    @Test
    @DisplayName("Consulta con un corte de 3 días atrás")
    void sendReEngagementEmailsShouldPassCutoffOfThreeDaysAgo() {
        // --- arrange ---
        givenNoInactiveUsers();
        // --- act ---
        sendReEngagementEmails();
        // --- assert ---
        thenCutoffIsThreeDaysAgo();
    }

    // --- arrange ---

    private AppUser inactiveUser(long id, String email, String token) {
        return AppUser.builder()
                .id(id).email(email).unsubscribeToken(token)
                .role(UserRole.USER).status(UserStatus.ACTIVE)
                .build();
    }

    private void givenTwoInactiveUsers() {
        when(userRepository.findUsersInactiveSince(any(Instant.class)))
                .thenReturn(List.of(
                        inactiveUser(1L, "user1@huly", "tok-1"),
                        inactiveUser(2L, "user2@huly", "tok-2")));
    }

    private void givenNoInactiveUsers() {
        when(userRepository.findUsersInactiveSince(any(Instant.class))).thenReturn(List.of());
    }

    // --- act ---

    private void sendReEngagementEmails() {
        cutoffLowerBound = Instant.now().minus(3, ChronoUnit.DAYS);
        reEngagementEmailScheduler.sendReEngagementEmails();
        cutoffUpperBound = Instant.now().minus(3, ChronoUnit.DAYS);
    }

    // --- assert ---

    private void thenReEngagementSentTo(String email, String token) {
        verify(emailPort).sendReEngagement(email, token);
    }

    private void thenNoReEngagementSent() {
        verify(emailPort, never()).sendReEngagement(any(), any());
    }

    private void thenCutoffIsThreeDaysAgo() {
        verify(userRepository).findUsersInactiveSince(argThat(
                cutoff -> !cutoff.isBefore(cutoffLowerBound) && !cutoff.isAfter(cutoffUpperBound)));
    }
}
