package com.huly.backend.domain.useCase.user;

import com.huly.backend.domain.model.user.UserAccountSettings;
import com.huly.backend.domain.repository.user.UserDetailDomainRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class GetUserAccountSettingsUseCaseTest {

    private UserDetailDomainRepository userDetailDomainRepository;
    private GetUserAccountSettingsUseCase useCase;

    @BeforeEach
    void setUp() {
        userDetailDomainRepository = mock(UserDetailDomainRepository.class);
        useCase = new GetUserAccountSettingsUseCase(userDetailDomainRepository);
    }

    @Test
    void execute_shouldReturnAccountSettingsForUser() {
        UserAccountSettings settings = new UserAccountSettings(
                "Mili",
                "mili@mail.com",
                LocalDate.of(2000, 1, 15)
        );
        when(userDetailDomainRepository.findAccountSettings(1L)).thenReturn(settings);

        UserAccountSettings response = useCase.execute(1L);

        assertThat(response).isEqualTo(settings);
    }
}
