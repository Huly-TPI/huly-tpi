package com.huly.backend.infrastructure.presentation.mapper.chatbot;

import com.huly.backend.domain.dto.admin.chatbot.ChatbotActivityDto;
import com.huly.backend.domain.dto.admin.chatbot.EmotionalCategoryDto;
import com.huly.backend.domain.dto.admin.chatbot.WellbeingDto;
import com.huly.backend.infrastructure.presentation.dto.dashboard.ActivityResponse;
import com.huly.backend.infrastructure.presentation.dto.dashboard.EmotionalCategoryResponse;
import com.huly.backend.infrastructure.presentation.dto.dashboard.WellbeingResponse;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class ChatbotDashboardPresentationMapper {

    public EmotionalCategoryResponse toResponse(EmotionalCategoryDto dto) {
        return new EmotionalCategoryResponse(
                dto.name(),
                dto.detections(),
                dto.detect(),
                dto.severity()
        );
    }

    public List<EmotionalCategoryResponse> toEmotionalCategoryResponseList(List<EmotionalCategoryDto> dtos) {
        return dtos.stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    public ActivityResponse toResponse(ChatbotActivityDto dto) {
        return new ActivityResponse(
                dto.name(),
                dto.type(),
                dto.pct()
        );
    }

    public List<ActivityResponse> toActivityResponseList(List<ChatbotActivityDto> dtos) {
        return dtos.stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    public WellbeingResponse toResponse(WellbeingDto dto) {
        return new WellbeingResponse(
                dto.points(),
                dto.labels()
        );
    }
}
