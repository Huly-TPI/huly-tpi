package com.huly.backend.domain.useCase.pushNotification;
import com.huly.backend.domain.model.user.AppUser;
import com.huly.backend.domain.model.enums.UserRole;
import com.huly.backend.domain.model.enums.UserStatus;
import com.huly.backend.domain.port.EmailPort;
import com.huly.backend.domain.repository.user.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
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

    @Test 
    void sendReEngagementEmails_shouldSendEmailToEachInactiveUser() { 
        AppUser user1 = AppUser.builder().id(1L).email("user1@huly").unsubscribeToken("tok-1").role
        (UserRole.USER).status(UserStatus.ACTIVE).build();

        AppUser user2 = AppUser.builder().id(2L).email("user2@huly").unsubscribeToken("tok-2").role
        (UserRole.USER).status(UserStatus.ACTIVE).build();
    
        when(userRepository.findUsersInactiveSince(any(Instant.class)))
            .thenReturn(List.of(user1, user2));

        reEngagementEmailScheduler.sendReEngagementEmails();

        verify(emailPort).sendReEngagement("user1@huly", "tok-1");
        verify(emailPort).sendReEngagement("user2@huly", "tok-2");
    }

    @Test 
    void sendReEngagementEmails_shouldDoNothing_whenNoInactiveUsers(){
        when(userRepository.findUsersInactiveSince(any(Instant.class))).thenReturn(List.of());
        reEngagementEmailScheduler.sendReEngagementEmails();
        verify(emailPort, never()).sendReEngagement(any(), any());
    }

    @Test 
    void sendReEngagmenetEmails_shouldPassCutoffOf3DaysAgo() { 
        when(userRepository.findUsersInactiveSince(any(Instant.class))).thenReturn(List.of());
       
        Instant before = Instant.now().minusSeconds(3 * 24 * 60 * 60);
        reEngagementEmailScheduler.sendReEngagementEmails(); 
        Instant after = Instant.now().minusSeconds(3 * 24 * 60 * 60); 

        verify(userRepository).findUsersInactiveSince(argThat(cutoff
            -> !cutoff.isBefore(before) && !cutoff.isAfter(after)
           ));
    }
}
