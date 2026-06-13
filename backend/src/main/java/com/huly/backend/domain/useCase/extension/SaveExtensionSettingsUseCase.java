package com.huly.backend.domain.useCase.extension;

import com.huly.backend.domain.model.extension.ExtensionSettings;
import com.huly.backend.domain.repository.extension.ExtensionSettingsRepository;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class SaveExtensionSettingsUseCase {
    private final ExtensionSettingsRepository settingsRepository;

    public void execute(Long userId, ExtensionSettings settings) {
        settingsRepository.save(userId, settings);
    }
}
