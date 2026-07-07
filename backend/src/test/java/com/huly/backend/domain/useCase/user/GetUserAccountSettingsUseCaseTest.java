package com.huly.backend.domain.useCase.user;

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
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GetUserAccountSettingsUseCaseTest {

    @Mock
    private UserDetailDomainRepository userDetailDomainRepository;

    private GetUserAccountSettingsUseCase useCase;

    @BeforeEach
    void setUp() {
        useCase = new GetUserAccountSettingsUseCase(userDetailDomainRepository);
    }

    @Test
    @DisplayName("Devuelve las opciones de cuenta del usuario")
    void executeReturnsAccountSettingsForUser() {
        UserAccountSettings expected = givenStoredAccountSettings(1L);

        UserAccountSettings result = getSettings(1L);

        thenSettingsMatch(result, expected);
    }

    // --- arrange ---

    private UserAccountSettings givenStoredAccountSettings(long userId) {
        UserAccountSettings settings = new UserAccountSettings(
                "Mili",
                "mili@mail.com",
                LocalDate.of(2000, 1, 15)
        );
        when(userDetailDomainRepository.findAccountSettings(userId)).thenReturn(settings);
        return settings;
    }

    // --- act ---

    private UserAccountSettings getSettings(long userId) {
        return useCase.execute(userId);
    }

    // --- assert ---

    private void thenSettingsMatch(UserAccountSettings result, UserAccountSettings expected) {
        assertThat(result).isEqualTo(expected);
    }
}
