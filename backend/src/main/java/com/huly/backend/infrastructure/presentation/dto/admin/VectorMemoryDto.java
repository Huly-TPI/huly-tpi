package com.huly.backend.infrastructure.presentation.dto.admin;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class VectorMemoryDto {
    private String id;
    private String content;
    private String sourceType;
    private String contentType;
    private String createdAt;
}
