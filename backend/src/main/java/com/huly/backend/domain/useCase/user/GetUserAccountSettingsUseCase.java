package com.huly.backend.domain.useCase.user;

import com.huly.backend.domain.model.user.UserAccountSettings;
import com.huly.backend.domain.repository.user.UserDetailDomainRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
public class GetUserAccountSettingsUseCase {

    private final UserDetailDomainRepository userDetailDomainRepository;

    @Transactional(readOnly = true)
    public UserAccountSettings execute(Long userId) {
        return userDetailDomainRepository.findAccountSettings(userId);
    }
}
