package com.huly.backend.domain.useCase.lead;

import com.huly.backend.domain.dto.lead.RegisterLeadRequest;
import com.huly.backend.domain.dto.lead.RegisterLeadResponse;
import com.huly.backend.domain.mapper.lead.RegisterLeadMapper;
import com.huly.backend.domain.model.user.AppUser;
import com.huly.backend.domain.model.enums.SourceAction;
import com.huly.backend.domain.model.enums.UserRole;
import com.huly.backend.domain.model.enums.UserStatus;
import com.huly.backend.domain.exception.DuplicateResourceException;
import com.huly.backend.domain.port.EmailPort;
import com.huly.backend.domain.repository.user.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
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

    private static final String EMAIL = "lead@huly.com";
    private static final String NICKNAME = "hulyuser";
    private static final Long USER_ID = 42L;

    @Mock
    private UserRepository userRepository;

    @Mock
    private EmailPort emailPort;

    private RegisterLeadUseCase registerLeadUseCase;

    @BeforeEach
    void setUp() {
        registerLeadUseCase = new RegisterLeadUseCase(userRepository, emailPort, new RegisterLeadMapper());
    }

    @Test
    @DisplayName("Guarda el usuario con rol LEAD y estado ACTIVE")
    void executeShouldSaveUserWithLeadRoleAndActiveStatus() {
        // --- arrange ---
        givenNewLead();
        givenPersistedUser();

        // --- act ---
        register(SourceAction.LANDING);

        // --- assert ---
        thenSavedUserIsActiveLead();
    }

    @Test
    @DisplayName("Guarda el detalle del lead con el nickname y la accion de origen")
    void executeShouldSaveLeadDetailWithNicknameAndSourceAction() {
        // --- arrange ---
        givenNewLead();
        givenPersistedUser();

        // --- act ---
        register(SourceAction.JOURNAL);

        // --- assert ---
        thenLeadDetailSavedWith(SourceAction.JOURNAL);
    }

    @Test
    @DisplayName("Envia el email de bienvenida al lead")
    void executeShouldSendWelcomeEmail() {
        // --- arrange ---
        givenNewLead();
        givenPersistedUser();

        // --- act ---
        register(SourceAction.LANDING);

        // --- assert ---
        thenWelcomeEmailSent();
    }

    @Test
    @DisplayName("Devuelve la respuesta con el id, email y nickname del lead registrado")
    void executeShouldReturnResponseWithLeadData() {
        // --- arrange ---
        givenNewLead();
        givenPersistedUser();

        // --- act ---
        RegisterLeadResponse result = register(SourceAction.LANDING);

        // --- assert ---
        thenResponseHasLeadData(result);
    }

    @Test
    @DisplayName("Lanza excepcion de conflicto cuando el email ya existe")
    void executeShouldThrowConflictWhenEmailAlreadyExists() {
        // --- arrange ---
        givenExistingLead();

        // --- assert ---
        thenRegisterThrowsDuplicate();
    }

    @Test
    @DisplayName("No guarda ni envia email cuando el email ya existe")
    void executeShouldNotSaveNorSendEmailWhenEmailAlreadyExists() {
        // --- arrange ---
        givenExistingLead();

        // --- assert ---
        thenRegisterThrowsDuplicate();
        thenNothingWasPersisted();
    }

    // --- arrange ---

    private void givenNewLead() {
        when(userRepository.existsByEmail(EMAIL)).thenReturn(false);
    }

    private void givenPersistedUser() {
        when(userRepository.save(any(AppUser.class)))
                .thenReturn(AppUser.builder().id(USER_ID).email(EMAIL).build());
    }

    private void givenExistingLead() {
        when(userRepository.existsByEmail(EMAIL)).thenReturn(true);
    }

    // --- act ---

    private RegisterLeadResponse register(SourceAction sourceAction) {
        return registerLeadUseCase.execute(new RegisterLeadRequest(EMAIL, NICKNAME, sourceAction));
    }

    // --- assert ---

    private void thenSavedUserIsActiveLead() {
        ArgumentCaptor<AppUser> captor = ArgumentCaptor.forClass(AppUser.class);
        verify(userRepository).save(captor.capture());
        AppUser saved = captor.getValue();
        assertThat(saved.getEmail()).isEqualTo(EMAIL);
        assertThat(saved.getRole()).isEqualTo(UserRole.LEAD);
        assertThat(saved.getStatus()).isEqualTo(UserStatus.ACTIVE);
    }

    private void thenLeadDetailSavedWith(SourceAction sourceAction) {
        verify(userRepository).saveLeadDetail(USER_ID, NICKNAME, sourceAction);
    }

    private void thenWelcomeEmailSent() {
        verify(emailPort).sendWelcomeLead(EMAIL, NICKNAME);
    }

    private void thenResponseHasLeadData(RegisterLeadResponse result) {
        assertThat(result.id()).isEqualTo(USER_ID);
        assertThat(result.email()).isEqualTo(EMAIL);
        assertThat(result.nickname()).isEqualTo(NICKNAME);
    }

    private void thenRegisterThrowsDuplicate() {
        assertThatThrownBy(() -> register(SourceAction.LANDING))
                .isInstanceOf(DuplicateResourceException.class)
                .hasMessageContaining("Ya existe un lead con email");
    }

    private void thenNothingWasPersisted() {
        verify(userRepository, never()).save(any());
        verify(userRepository, never()).saveLeadDetail(any(), any(), any());
        verify(emailPort, never()).sendWelcomeLead(any(), any());
    }
}
