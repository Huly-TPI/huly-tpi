package com.huly.backend.infrastructure.presentation.dto.admin;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class TopAppResponse {
    private String domain;
    private int totalActiveSeconds;
}
