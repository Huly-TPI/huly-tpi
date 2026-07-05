package com.huly.backend.domain.useCase.user;

import com.huly.backend.domain.exception.BusinessRuleException;
import com.huly.backend.domain.model.user.UserAccountSettings;
import com.huly.backend.domain.repository.user.UserDetailDomainRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class UpdateUserAccountSettingsUseCaseTest {

    private UserDetailDomainRepository userDetailDomainRepository;
    private UpdateUserAccountSettingsUseCase useCase;

    @BeforeEach
    void setUp() {
        userDetailDomainRepository = mock(UserDetailDomainRepository.class);
        useCase = new UpdateUserAccountSettingsUseCase(userDetailDomainRepository);
    }

    @Test
    void execute_shouldTrimAndPersistAllowedFields() {
        UserAccountSettings normalized = new UserAccountSettings(
                "Mili",
                null,
                LocalDate.of(2000, 1, 15)
        );
        UserAccountSettings saved = new UserAccountSettings(
                "Mili",
                "mili@mail.com",
                LocalDate.of(2000, 1, 15)
        );
        when(userDetailDomainRepository.updateAccountSettings(eq(1L), eq(normalized))).thenReturn(saved);

        UserAccountSettings response = useCase.execute(
                1L,
                new UserAccountSettings(" Mili ", null, LocalDate.of(2000, 1, 15))
        );

        assertThat(response).isEqualTo(saved);
        verify(userDetailDomainRepository).updateAccountSettings(1L, normalized);
    }

    @Test
    void execute_shouldRejectBlankName() {
        assertThatThrownBy(() -> useCase.execute(1L, new UserAccountSettings(" ", null, null)))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessage("El nombre es obligatorio");
    }

    @Test
    void execute_shouldRejectTooLongName() {
        String longName = "a".repeat(81);

        assertThatThrownBy(() -> useCase.execute(1L, new UserAccountSettings(longName, null, null)))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessage("El nombre no puede superar los 80 caracteres");
    }
}
