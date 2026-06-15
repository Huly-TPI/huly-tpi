package com.huly.backend.infrastructure.presentation.dto.extension;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ExtensionMetricRequest {
    @NotBlank
    private String domain;
    
    private int activeSeconds;
    private int scrollCount;
    private int modalsShown;
    private int redirects;
}
