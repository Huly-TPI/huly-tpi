package com.huly.backend.infrastructure.presentation.dto.extension;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Getter;
import java.util.List;

@Getter
@Builder
public class ExtensionSettingsResponse {
    private boolean enabled;
    
    @JsonProperty("pauseIntervalMinutes")
    private int pauseIntervalMinutes;
    
    @JsonProperty("gardenUrl")
    private String gardenUrl;
    
    @JsonProperty("backendUrl")
    private String backendUrl;

    @JsonProperty("monitoredDomains")
    private List<String> monitoredDomains;

    @JsonProperty("dataSharingConsent")
    private boolean dataSharingConsent;

    @JsonProperty("userName")
    private String userName;

    @JsonProperty("termsAndConditions")
    private String termsAndConditions;
}
