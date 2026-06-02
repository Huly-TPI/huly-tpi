package com.huly.backend.presentation.controller;

import com.huly.backend.domain.model.CloudRecommendation;
import com.huly.backend.domain.service.vector.UserVectorMemoryService;
import com.huly.backend.domain.useCase.GetCloudRecommendationUseCase;
import com.huly.backend.exception.NotFoundException;
import com.huly.backend.infrastructure.repository.entity.AppUserEntity;
import com.huly.backend.infrastructure.repository.jpaRepository.interfaces.AppUserRepository;
import com.huly.backend.presentation.dto.CloudRecommendationRequest;
import com.huly.backend.presentation.dto.CloudRecommendationResponse;
import com.huly.backend.presentation.dto.CloudThoughtRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

/**
 * Controlador del ejercicio de "nubes emocionales".
 * El flujo es:
 *  1. El usuario escribe pensamientos/emociones que quiere soltar → POST /thought
 *  2. Esos pensamientos se guardan en memoria vectorial para contexto futuro del chatbot.
 *  3. Luego pide una recomendación de actividad basada en esos pensamientos → POST /recommendation
 *  4. La IA analiza los pensamientos y sugiere una actividad de bienestar (diario, respiración, etc.).
 */
@RestController
@RequestMapping("/api/clouds")
@RequiredArgsConstructor
public class CloudController {

    private final GetCloudRecommendationUseCase getCloudRecommendationUseCase;
    private final UserVectorMemoryService userVectorMemoryService;
    private final AppUserRepository appUserRepository;

    /**
     * Guarda un pensamiento suelto en la memoria vectorial del usuario.
     * El pensamiento queda indexado semánticamente para que el chatbot pueda
     * recordarlo y contextualizarlo en futuras conversaciones.
     * Cada guardado genera un UUID nuevo como ID de sesión de nube.
     */
    @PostMapping("/thought")
    public ResponseEntity<Void> saveThought(@RequestBody @Valid CloudThoughtRequest request) {
        // Obtiene el email del usuario autenticado desde el contexto de seguridad
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        AppUserEntity user = appUserRepository.findByEmail(email)
                .orElseThrow(() -> new NotFoundException("Usuario no encontrado"));

        userVectorMemoryService.rememberGuidedCloudInput(user.getId(), UUID.randomUUID().toString(), request.thought());
        return ResponseEntity.noContent().build();
    }

    /**
     * Analiza una lista de pensamientos con IA y devuelve una recomendación de actividad.
     * La IA clasifica el estado emocional y sugiere: diario, nubes, respiración o burbujas.
     * No requiere userId porque la recomendación es stateless (se basa solo en los pensamientos enviados).
     */
    @PostMapping("/recommendation")
    public ResponseEntity<CloudRecommendationResponse> getRecommendation(
            @RequestBody @Valid CloudRecommendationRequest request
    ) {
        CloudRecommendation recommendation = getCloudRecommendationUseCase.execute(request.thoughts());
        return ResponseEntity.ok(new CloudRecommendationResponse(
                recommendation.activityType(),
                recommendation.actionId(),
                recommendation.title(),
                recommendation.description(),
                recommendation.redirectUrl()
        ));
    }
}
