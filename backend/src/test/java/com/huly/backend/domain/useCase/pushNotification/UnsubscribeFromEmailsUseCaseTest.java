package com.huly.backend.domain.useCase.pushNotification;

import com.huly.backend.domain.model.user.AppUser;
import com.huly.backend.domain.repository.user.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UnsubscribeFromEmailsUseCaseTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UnsubscribeFromEmailsUseCase useCase;

    @Test
    void execute_shouldDisableEmailsAndReturnTrue_whenTokenIsValid() {
        AppUser user = AppUser.builder().id(7L).build();
        when(userRepository.findByUnsubscribeToken("tok-123")).thenReturn(Optional.of(user));

        boolean result = useCase.execute("tok-123");

        assertThat(result).isTrue();
        verify(userRepository).disableReengagementEmails(7L);
    }

    @Test
    void execute_shouldReturnFalse_whenTokenIsInvalid() {
        when(userRepository.findByUnsubscribeToken("bad")).thenReturn(Optional.empty());

        boolean result = useCase.execute("bad");

        assertThat(result).isFalse();
        verify(userRepository, never()).disableReengagementEmails(anyLong());
    }


}
