package com.huly.backend.presentation.controller;
import com.huly.backend.domain.useCase.onboarding.CompleteProfileOnboardingUseCase;
import com.huly.backend.domain.useCase.onboarding.GenerateOnboardingOptionsUseCase;
import com.huly.backend.presentation.dto.onboarding.CompleteOnboardingRequest;
import com.huly.backend.presentation.dto.onboarding.GenerateOptionsRequest;
import com.huly.backend.presentation.dto.onboarding.GenerateOptionsResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;



/**
 * Controlador del flujo de onboarding de perfil.
 * El onboarding guía al usuario recién registrado para que defina sus 3 objetivos principales.
 * La IA genera opciones contextuales en cada paso basándose en la respuesta anterior.
 *
 * Flujo:
 *  1. POST /generate-options → genera opciones de selección para el paso actual (step 1, 2 o 3).
 *  2. POST /complete → guarda las 3 respuestas finales y las indexa en memoria vectorial.
 *
 * Una vez completado, profileOnBoardingCompleted=true se incluye en el JWT para el frontend.
 */
@RestController
@RequestMapping("/api/onboarding")
@RequiredArgsConstructor
public class OnboardingController {

    private final GenerateOnboardingOptionsUseCase generateOnboardingOptionsUseCase;
    private final CompleteProfileOnboardingUseCase completeProfileOnboardingUseCase;

    /**
     * Genera opciones de onboarding para el paso indicado usando IA.
     * La respuesta anterior (previousAnswer) permite que la IA contextualice las siguientes opciones.
     * El parámetro userDetails (inyectado por Spring Security) no se usa aquí actualmente.
     */
    @PostMapping("/generate-options")
    public ResponseEntity<GenerateOptionsResponse> generateOptions(
            @Valid @RequestBody GenerateOptionsRequest request,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        List<String> options = generateOnboardingOptionsUseCase.execute(request.step(), request.previousAnswer());
        return ResponseEntity.ok(new GenerateOptionsResponse(options));
    }

    /**
     * Finaliza el onboarding guardando las 3 respuestas del usuario.
     * Las respuestas se indexan en la memoria vectorial para que el chatbot
     * pueda recordar los objetivos del usuario en futuras conversaciones.
     * El email del usuario se obtiene de userDetails (ya autenticado via JWT).
     */
    @PostMapping("/complete")
    public ResponseEntity<Void> completeOnboarding(
            @Valid @RequestBody CompleteOnboardingRequest request,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        completeProfileOnboardingUseCase.execute(userDetails.getUsername(), request.answer1(), request.answer2(), request.answer3());
        return ResponseEntity.noContent().build();
    }
}
