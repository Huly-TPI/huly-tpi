package com.huly.backend.domain.model.admin;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class TopAppStats {
    private String domain;
    private int totalActiveSeconds;
}
