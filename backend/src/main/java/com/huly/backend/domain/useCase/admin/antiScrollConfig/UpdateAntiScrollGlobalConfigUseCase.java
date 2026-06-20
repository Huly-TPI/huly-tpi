package com.huly.backend.domain.useCase.admin.antiScrollConfig;

import com.huly.backend.domain.model.extension.AntiScrollGlobalConfig;
import com.huly.backend.domain.repository.extension.AntiScrollGlobalConfigRepository;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class UpdateAntiScrollGlobalConfigUseCase {
    private final AntiScrollGlobalConfigRepository antiScrollConfigRepository;

    public void execute(UpdateAntiScrollGlobalConfigRequest request) {
        AntiScrollGlobalConfig existing = antiScrollConfigRepository.findFirst()
                .orElse(AntiScrollGlobalConfig.builder().build());

        AntiScrollGlobalConfig updated = AntiScrollGlobalConfig.builder()
                .id(existing.getId())
                .defaultPauseIntervalMinutes(request.defaultPauseIntervalMinutes())
                .termsAndConditions(request.termsAndConditions())
                .build();

        antiScrollConfigRepository.save(updated);
    }
}
