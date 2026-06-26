package com.huly.backend.domain.useCase.extension;

import com.huly.backend.domain.dto.extension.SaveUserAntiScrollSettingsRequest;
import com.huly.backend.domain.dto.extension.SaveUserAntiScrollSettingsResponse;
import com.huly.backend.domain.mapper.extension.SaveUserAntiScrollSettingsMapper;
import com.huly.backend.domain.model.extension.UserAntiScrollSettings;
import com.huly.backend.domain.repository.extension.UserAntiScrollSettingsRepository;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class SaveUserAntiScrollSettingsUseCase {
    private final UserAntiScrollSettingsRepository settingsRepository;
    private final SaveUserAntiScrollSettingsMapper mapper;

    public SaveUserAntiScrollSettingsResponse execute(SaveUserAntiScrollSettingsRequest request) {
        UserAntiScrollSettings settings = mapper.toModel(request);
        settingsRepository.save(request.userId(), settings);
        return mapper.toResponse();
    }
}
