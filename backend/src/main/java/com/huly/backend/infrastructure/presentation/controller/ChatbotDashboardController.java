package com.huly.backend.infrastructure.presentation.controller;

import com.huly.backend.domain.useCase.admin.chatbot.GetEmotionalCategoriesUseCase;
import com.huly.backend.domain.useCase.admin.chatbot.GetWellbeingUseCase;
import com.huly.backend.infrastructure.presentation.dto.dashboard.EmotionalCategoryResponse;
import com.huly.backend.infrastructure.presentation.dto.dashboard.WellbeingResponse;
import com.huly.backend.infrastructure.presentation.mapper.chatbot.ChatbotDashboardPresentationMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/admin/chatbot")
public class ChatbotDashboardController {

    private final GetEmotionalCategoriesUseCase getEmotionalCategoriesUseCase;
    private final GetWellbeingUseCase getWellbeingUseCase;
    private final ChatbotDashboardPresentationMapper mapper;

    @GetMapping("/emotional-categories")
    public ResponseEntity<List<EmotionalCategoryResponse>> getEmotionalCategories() {
        return ResponseEntity.ok(mapper.toEmotionalCategoryResponseList(
                getEmotionalCategoriesUseCase.execute()
        ));
    }

    @GetMapping("/wellbeing")
    public ResponseEntity<WellbeingResponse> getWellbeing() {
        return ResponseEntity.ok(mapper.toResponse(
                getWellbeingUseCase.execute()
        ));
    }

}
