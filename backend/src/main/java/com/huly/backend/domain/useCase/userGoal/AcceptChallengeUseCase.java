package com.huly.backend.domain.useCase.userGoal;

import com.huly.backend.domain.model.AppUser;
import com.huly.backend.domain.model.UserGoal;
import com.huly.backend.domain.model.enums.GoalStatus;
import com.huly.backend.domain.repository.UserGoalRepository;
import com.huly.backend.domain.repository.UserRepository;
import com.huly.backend.exception.NotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;

/**
 * Caso de uso: aceptar un reto sugerido por el chatbot y guardarlo como meta del usuario.
 * El usuario se identifica por email (extraído del JWT), no por ID en el body,
 * lo que garantiza que el usuario solo puede crear metas para sí mismo.
 * La meta se crea con estado PENDING (aún no completada).
 */
@Service
@RequiredArgsConstructor
public class AcceptChallengeUseCase {

    private final UserRepository userRepository;
    private final UserGoalRepository userGoalRepository;

    public UserGoal execute(String email, String title, String description, Long activityId) {
        // Resuelve el usuario desde su email (proveniente del JWT, no del body)
        AppUser user = userRepository.findByEmail(email)
                .orElseThrow(() -> new NotFoundException("Usuario no encontrado"));

        UserGoal userGoal = UserGoal.builder()
                .userId(user.getId())
                .title(title)
                .description(description)
                .activityId(activityId)
                .status(GoalStatus.PENDING) // Todas las metas comienzan pendientes
                .createdAt(Instant.now())
                .build();

        return userGoalRepository.save(userGoal);
    }
}
