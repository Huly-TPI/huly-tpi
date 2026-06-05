package com.huly.backend.domain.useCase;

import com.huly.backend.domain.model.AppUser;
import com.huly.backend.domain.model.enums.SourceAction;
import com.huly.backend.domain.model.enums.UserRole;
import com.huly.backend.domain.model.enums.UserStatus;
import com.huly.backend.domain.port.EmailPort;
import com.huly.backend.domain.repository.UserRepository;
import com.huly.backend.domain.useCase.lead.RegisterLeadUseCase;
import com.huly.backend.infrastructure.presentation.exception.ConflictException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RegisterLeadUseCaseTest {

    @Mock private UserRepository userRepository;
    @Mock private EmailPort emailPort;

    @InjectMocks private RegisterLeadUseCase registerLeadUseCase;

    @Test
    void execute_shouldSaveUserWithLeadRoleAndActiveStatus() {
        when(userRepository.existsByEmail("lead@huly.com")).thenReturn(false);
        when(userRepository.save(any(AppUser.class))).thenReturn(AppUser.builder().id(1L).build());

        ArgumentCaptor<AppUser> captor = ArgumentCaptor.forClass(AppUser.class);
        registerLeadUseCase.execute("lead@huly.com", "hulyuser", SourceAction.LANDING);

        verify(userRepository).save(captor.capture());
        AppUser saved = captor.getValue();
        assertThat(saved.getEmail()).isEqualTo("lead@huly.com");
        assertThat(saved.getRole()).isEqualTo(UserRole.LEAD);
        assertThat(saved.getStatus()).isEqualTo(UserStatus.ACTIVE);
    }

    @Test
    void execute_shouldSaveLeadDetailWithNicknameAndSourceAction() {
        when(userRepository.existsByEmail("lead@huly.com")).thenReturn(false);
        when(userRepository.save(any(AppUser.class))).thenReturn(AppUser.builder().id(42L).build());

        registerLeadUseCase.execute("lead@huly.com", "hulyuser", SourceAction.JOURNAL);

        verify(userRepository).saveLeadDetail(42L, "hulyuser", SourceAction.JOURNAL);
    }

    @Test
    void execute_shouldSendWelcomeEmail() {
        when(userRepository.existsByEmail("lead@huly.com")).thenReturn(false);
        when(userRepository.save(any(AppUser.class))).thenReturn(AppUser.builder().id(1L).build());

        registerLeadUseCase.execute("lead@huly.com", "hulyuser", SourceAction.LANDING);

        verify(emailPort).sendWelcomeLead("lead@huly.com", "hulyuser");
    }

    @Test
    void execute_shouldThrowConflictException_whenEmailAlreadyExists() {
        when(userRepository.existsByEmail("existing@huly.com")).thenReturn(true);

        assertThatThrownBy(() -> registerLeadUseCase.execute("existing@huly.com", "hulyuser", SourceAction.LANDING))
                .isInstanceOf(ConflictException.class)
                .hasMessageContaining("Ya existe en lead con email");
    }

    @Test
    void execute_shouldNotSaveNorSendEmail_whenEmailAlreadyExists() {
        when(userRepository.existsByEmail("existing@huly.com")).thenReturn(true);

        assertThatThrownBy(() -> registerLeadUseCase.execute("existing@huly.com", "hulyuser", SourceAction.LANDING))
                .isInstanceOf(ConflictException.class);

        verify(userRepository, never()).save(any());
        verify(userRepository, never()).saveLeadDetail(any(), any(), any());
        verify(emailPort, never()).sendWelcomeLead(any(), any());
    }
}
