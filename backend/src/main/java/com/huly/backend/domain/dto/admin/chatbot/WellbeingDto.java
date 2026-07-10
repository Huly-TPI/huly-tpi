package com.huly.backend.domain.dto.admin.chatbot;

import java.util.List;

public record WellbeingDto(
        List<Integer> points,
        List<String> labels
) {}
