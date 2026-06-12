package com.huly.backend.domain.model.extension;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ExtensionMetric {
    private String domain;
    private int activeSeconds;
    private int scrollCount;
    private int modalsShown;
    private int redirects;
}
