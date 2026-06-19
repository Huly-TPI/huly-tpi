package com.huly.backend.domain.useCase.extension;

import com.huly.backend.domain.model.extension.UserAntiScrollSettings;
import com.huly.backend.domain.repository.extension.UserAntiScrollSettingsRepository;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class SaveUserAntiScrollSettingsUseCase {
    private final UserAntiScrollSettingsRepository settingsRepository;

    public void execute(Long userId, UserAntiScrollSettings settings) {
        settingsRepository.save(userId, settings);
    }
}
