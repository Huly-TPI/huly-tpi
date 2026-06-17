package com.huly.backend.domain.useCase.admin.antiScrollConfig;

import com.huly.backend.domain.model.extension.AntiScrollConfig;
import com.huly.backend.domain.repository.extension.AntiScrollConfigRepository;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class UpdateAntiScrollConfigUseCase {
    private final AntiScrollConfigRepository antiScrollConfigRepository;

    public void execute(UpdateAntiScrollConfigRequest request) {
        AntiScrollConfig existing = antiScrollConfigRepository.findFirst()
                .orElse(AntiScrollConfig.builder().build());

        AntiScrollConfig updated = AntiScrollConfig.builder()
                .id(existing.getId())
                .defaultPauseIntervalMinutes(request.defaultPauseIntervalMinutes())
                .termsAndConditions(request.termsAndConditions())
                .build();

        antiScrollConfigRepository.save(updated);
    }
}
