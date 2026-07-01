package com.huly.backend.domain.service.user;

import com.huly.backend.domain.repository.user.UserDetailDomainRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Optional;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserActivityServiceTest {

    private static final Long USER_ID = 10L;
    private static final LocalDate TODAY = LocalDate.of(2026, 6, 12);

    @Mock
    private UserDetailDomainRepository userDetailDomainRepository;

    @InjectMocks
    private UserActivityService service;

    @Test
    void registerActivity_shouldAdvanceLastLogin_whenNoComebackPending() {
        when(userDetailDomainRepository.findLastLoginDate(USER_ID)).thenReturn(Optional.of(TODAY.minusDays(3)));

        service.registerActivity(USER_ID, TODAY);

        verify(userDetailDomainRepository).updateLastLoginDate(USER_ID, TODAY);
    }

    @Test
    void registerActivity_shouldAdvanceLastLogin_whenNeverSeen() {
        when(userDetailDomainRepository.findLastLoginDate(USER_ID)).thenReturn(Optional.empty());

        service.registerActivity(USER_ID, TODAY);

        verify(userDetailDomainRepository).updateLastLoginDate(USER_ID, TODAY);
    }

    @Test
    void registerActivity_shouldNotAdvanceLastLogin_whenComebackPending() {
        // Brecha >= umbral (10 días): hay un comeback pendiente, no hay que borrar la brecha.
        when(userDetailDomainRepository.findLastLoginDate(USER_ID)).thenReturn(Optional.of(TODAY.minusDays(15)));

        service.registerActivity(USER_ID, TODAY);

        verify(userDetailDomainRepository, never()).updateLastLoginDate(anyLong(), any());
    }
}
