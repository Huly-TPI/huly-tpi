package com.huly.backend.domain.useCase.user;

import com.huly.backend.domain.exception.BusinessRuleException;
import com.huly.backend.domain.model.user.UserAccountSettings;
import com.huly.backend.domain.repository.user.UserDetailDomainRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UpdateUserAccountSettingsUseCaseTest {

    private static final long USER_ID = 1L;
    private static final LocalDate BIRTH_DATE = LocalDate.of(2000, 1, 15);

    @Mock
    private UserDetailDomainRepository userDetailDomainRepository;

    private UpdateUserAccountSettingsUseCase useCase;

    @BeforeEach
    void setUp() {
        useCase = new UpdateUserAccountSettingsUseCase(userDetailDomainRepository);
    }

    @Test
    @DisplayName("Recorta y persiste solo los campos permitidos")
    void executeTrimsAndPersistsAllowedFields() {
        UserAccountSettings saved = givenPersistedTrimmedName();

        UserAccountSettings result = update(USER_ID, " Mili ", null, BIRTH_DATE);

        thenReturnedSettingsAre(result, saved);
        thenPersistedNormalizedName(USER_ID, "Mili", null, BIRTH_DATE);
    }

    @Test
    @DisplayName("Rechaza el nombre en blanco")
    void executeRejectsBlankName() {
        thenUpdateRejectsBlankName();
    }

    @Test
    @DisplayName("Rechaza el nombre nulo")
    void executeRejectsNullName() {
        thenUpdateRejectsNullName();
    }

    @Test
    @DisplayName("Rechaza el nombre demasiado largo")
    void executeRejectsTooLongName() {
        thenUpdateRejectsTooLongName();
    }

    // --- arrange ---

    private UserAccountSettings givenPersistedTrimmedName() {
        UserAccountSettings normalized = new UserAccountSettings("Mili", null, BIRTH_DATE);
        UserAccountSettings saved = new UserAccountSettings("Mili", "mili@mail.com", BIRTH_DATE);
        when(userDetailDomainRepository.updateAccountSettings(eq(USER_ID), eq(normalized))).thenReturn(saved);
        return saved;
    }

    // --- act ---

    private UserAccountSettings update(long userId, String name, String email, LocalDate birthDate) {
        return useCase.execute(userId, new UserAccountSettings(name, email, birthDate));
    }

    // --- assert ---

    private void thenReturnedSettingsAre(UserAccountSettings result, UserAccountSettings expected) {
        assertThat(result).isEqualTo(expected);
    }

    private void thenPersistedNormalizedName(long userId, String name, String email, LocalDate birthDate) {
        verify(userDetailDomainRepository).updateAccountSettings(userId, new UserAccountSettings(name, email, birthDate));
    }

    private void thenUpdateRejectsBlankName() {
        assertThatThrownBy(() -> useCase.execute(USER_ID, new UserAccountSettings(" ", null, null)))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessage("El nombre es obligatorio");
    }

    private void thenUpdateRejectsNullName() {
        assertThatThrownBy(() -> useCase.execute(USER_ID, new UserAccountSettings(null, null, null)))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessage("El nombre es obligatorio");
    }

    private void thenUpdateRejectsTooLongName() {
        String longName = "a".repeat(81);
        assertThatThrownBy(() -> useCase.execute(USER_ID, new UserAccountSettings(longName, null, null)))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessage("El nombre no puede superar los 80 caracteres");
    }
}
