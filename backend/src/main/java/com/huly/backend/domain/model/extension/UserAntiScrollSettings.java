package com.huly.backend.domain.model.extension;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class UserAntiScrollSettings {
    private boolean enabled;
    private int pauseIntervalSeconds;
    private List<String> monitoredDomains;
    private boolean dataSharingConsent;
}
