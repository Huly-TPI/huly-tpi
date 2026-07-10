package com.huly.backend.domain.useCase.auth;

import com.huly.backend.domain.exception.DuplicateResourceException;
import com.huly.backend.domain.model.auth.AuthTokens;
import com.huly.backend.domain.model.enums.UserRole;
import com.huly.backend.domain.model.enums.UserStatus;
import com.huly.backend.domain.model.user.AppUser;
import com.huly.backend.domain.port.PasswordHasherPort;
import com.huly.backend.domain.repository.user.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RegisterUseCaseTest {

    private static final String EMAIL = "new@huly.com";
    private static final String EXISTING_EMAIL = "existing@huly.com";
    private static final String RAW_PASSWORD = "rawPass";
    private static final String NAME = "Juan";
    private static final LocalDate BIRTH_DATE = LocalDate.of(2000, 1, 1);

    @Mock private UserRepository userRepository;
    @Mock private PasswordHasherPort passwordHasherPort;
    @Mock private LoginUseCase loginUseCase;

    @InjectMocks private RegisterUseCase registerUseCase;

    @Test
    @DisplayName("Guarda el usuario con nombre, rol USER y estado ACTIVE")
    void executeShouldSaveUserWithNameRoleUserAndStatusActive() {
        givenEmailAvailable();
        givenPasswordEncoded("encodedPass");
        givenUserSaved();
        givenLoginSucceeds();

        register();

        thenSavedUserHasCorrectData();
    }

    @Test
    @DisplayName("Lanza DuplicateResource cuando el email ya está en uso")
    void executeShouldThrowConflictWhenEmailAlreadyExists() {
        givenEmailTaken();

        thenRegisterThrowsDuplicate();
    }

    @Test
    @DisplayName("Codifica la contraseña antes de guardar")
    void executeShouldEncodePasswordBeforeSaving() {
        givenEmailAvailable();
        givenPasswordEncoded("hashedPassword");
        givenUserSaved();
        givenLoginSucceeds();

        register();

        thenPasswordEncoded();
    }

    @Test
    @DisplayName("Devuelve los tokens generados por el login")
    void executeShouldReturnTokensFromLogin() {
        AuthTokens expected = expectedTokens();
        givenEmailAvailable();
        givenPasswordEncoded("encodedPass");
        givenUserSaved();
        givenLoginReturns(expected);

        AuthTokens result = register();

        thenResultEquals(result, expected);
    }

    // --- arrange ---

    private void givenEmailAvailable() {
        when(userRepository.existsByEmail(EMAIL)).thenReturn(false);
    }

    private void givenEmailTaken() {
        when(userRepository.existsByEmail(EXISTING_EMAIL)).thenReturn(true);
    }

    private void givenPasswordEncoded(String encoded) {
        when(passwordHasherPort.encode(RAW_PASSWORD)).thenReturn(encoded);
    }

    private void givenUserSaved() {
        when(userRepository.save(any(AppUser.class))).thenReturn(AppUser.builder().id(1L).build());
    }

    private void givenLoginSucceeds() {
        when(loginUseCase.execute(EMAIL, RAW_PASSWORD)).thenReturn(AuthTokens.builder().build());
    }

    private void givenLoginReturns(AuthTokens tokens) {
        when(loginUseCase.execute(EMAIL, RAW_PASSWORD)).thenReturn(tokens);
    }

    private AuthTokens expectedTokens() {
        return AuthTokens.builder()
                .accessToken("tok").refreshToken("ref").role(UserRole.USER)
                .build();
    }

    // --- act ---

    private AuthTokens register() {
        return registerUseCase.execute(EMAIL, RAW_PASSWORD, NAME, BIRTH_DATE);
    }

    // --- assert ---

    private void thenSavedUserHasCorrectData() {
        ArgumentCaptor<AppUser> captor = ArgumentCaptor.forClass(AppUser.class);
        verify(userRepository).save(captor.capture());
        AppUser saved = captor.getValue();
        assertThat(saved.getName()).isEqualTo(NAME);
        assertThat(saved.getEmail()).isEqualTo(EMAIL);
        assertThat(saved.getPassword()).isEqualTo("encodedPass");
        assertThat(saved.getBirthDate()).isEqualTo(BIRTH_DATE);
        assertThat(saved.getRole()).isEqualTo(UserRole.USER);
        assertThat(saved.getStatus()).isEqualTo(UserStatus.ACTIVE);
    }

    private void thenRegisterThrowsDuplicate() {
        assertThatThrownBy(() -> registerUseCase.execute(EXISTING_EMAIL, RAW_PASSWORD, NAME, BIRTH_DATE))
                .isInstanceOf(DuplicateResourceException.class)
                .hasMessageContaining("el email esta en uso");
    }

    private void thenPasswordEncoded() {
        verify(passwordHasherPort).encode(RAW_PASSWORD);
    }

    private void thenResultEquals(AuthTokens result, AuthTokens expected) {
        assertThat(result).isEqualTo(expected);
    }
}
