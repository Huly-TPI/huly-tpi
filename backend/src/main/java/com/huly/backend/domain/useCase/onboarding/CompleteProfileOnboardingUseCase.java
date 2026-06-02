package com.huly.backend.domain.useCase.onboarding;

import com.huly.backend.domain.model.AppUser;
import com.huly.backend.domain.repository.UserDetailDomainRepository;
import com.huly.backend.domain.repository.UserRepository;
import com.huly.backend.exception.NotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.huly.backend.domain.service.vector.UserVectorMemoryService;


/**
 * Caso de uso: finalizar el onboarding de perfil del usuario.
 * Persiste las 3 respuestas del onboarding en el detalle del usuario (BD relacional)
 * y las indexa en la memoria vectorial para que el chatbot recuerde los objetivos del usuario.
 *
 * El fallo en la indexación vectorial NO interrumpe el flujo: el onboarding se completa
 * igual aunque la memoria vectorial falle temporalmente.
 */
@Service
@RequiredArgsConstructor
public class CompleteProfileOnboardingUseCase {

    private final UserRepository userRepository;
    private final UserDetailDomainRepository userDetailDomainRepository;
    private final UserVectorMemoryService userVectorMemoryService;

    @Transactional
    public void execute(String email, String answer1, String answer2, String answer3) {
        AppUser user = userRepository.findByEmail(email)
                .orElseThrow(() -> new NotFoundException("Usuario no encontrado: " + email));

        // Persiste las respuestas en BD y marca el onboarding como completado
        userDetailDomainRepository.completeOnboarding(user.getId(), answer1, answer2, answer3);

        // Indexa los objetivos en memoria vectorial (fallo no-crítico: el onboarding ya fue guardado)
        try {
            userVectorMemoryService.rememberOnboardingGoals(user.getId(), answer1, answer2, answer3);
        } catch (Exception e) {
            // Si la memoria vectorial falla, el onboarding igual se completa correctamente
        }
    }
}
