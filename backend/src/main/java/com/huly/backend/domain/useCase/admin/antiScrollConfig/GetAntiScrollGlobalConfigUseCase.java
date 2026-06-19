package com.huly.backend.domain.useCase.admin.antiScrollConfig;

import com.huly.backend.domain.model.extension.AntiScrollGlobalConfig;
import com.huly.backend.domain.repository.extension.AntiScrollGlobalConfigRepository;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class GetAntiScrollGlobalConfigUseCase {
    private static final String DEFAULT_ANTI_SCROLL_TERMS =
            "El modo anti-scroll es simplemente una herramienta para acompa\u00f1arte cuando sientas que necesit\u00e1s frenar un poco. No hay reglas estrictas ni metas que cumplir. Activalo cuando quieras priorizar tu concentraci\u00f3n o desconectar del ruido, y apagalo cuando tengas ganas de explorar libremente. \u00a1Cero presiones, el ritmo lo marc\u00e1s vos!";

    private final AntiScrollGlobalConfigRepository antiScrollConfigRepository;

    public GetAntiScrollGlobalConfigResponse execute() {
        AntiScrollGlobalConfig config = antiScrollConfigRepository.findFirst()
                .orElse(AntiScrollGlobalConfig.builder()
                        .defaultPauseIntervalMinutes(20)
                        .termsAndConditions(DEFAULT_ANTI_SCROLL_TERMS)
                        .build());

        return new GetAntiScrollGlobalConfigResponse(
                config.getDefaultPauseIntervalMinutes(),
                config.getTermsAndConditions()
        );
    }
}
