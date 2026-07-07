package com.huly.backend.domain.useCase.user;

import com.huly.backend.domain.exception.BusinessRuleException;
import com.huly.backend.domain.model.user.UserAccountSettings;
import com.huly.backend.domain.repository.user.UserDetailDomainRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
public class UpdateUserAccountSettingsUseCase {

    private static final int MAX_NAME_LENGTH = 80;

    private final UserDetailDomainRepository userDetailDomainRepository;

    @Transactional
    public UserAccountSettings execute(Long userId, UserAccountSettings accountSettings) {
        String name = normalizeRequired(accountSettings.name(), "El nombre es obligatorio");

        if (name.length() > MAX_NAME_LENGTH) {
            throw new BusinessRuleException("El nombre no puede superar los 80 caracteres");
        }

        return userDetailDomainRepository.updateAccountSettings(
                userId,
                new UserAccountSettings(name, accountSettings.email(), accountSettings.birthDate())
        );
    }

    private String normalizeRequired(String value, String errorMessage) {
        if (value == null || value.trim().isEmpty()) {
            throw new BusinessRuleException(errorMessage);
        }
        return value.trim();
    }
}
