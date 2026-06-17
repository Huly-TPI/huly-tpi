package com.huly.backend.infrastructure.presentation.dto.extension;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;
import java.util.List;

@Getter
@Setter
public class ExtensionSettingsRequest {
    private boolean enabled;
    
    @JsonProperty("pauseIntervalSeconds")
    private int pauseIntervalSeconds;
    
    @JsonProperty("monitoredDomains")
    private List<String> monitoredDomains;

    @JsonProperty("dataSharingConsent")
    private boolean dataSharingConsent;
}
