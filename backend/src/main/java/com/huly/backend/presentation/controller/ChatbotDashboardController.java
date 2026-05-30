package com.huly.backend.presentation.controller;

import com.huly.backend.presentation.dto.dashboard.ActivityResponse;
import com.huly.backend.presentation.dto.dashboard.EmotionalCategoryResponse;
import com.huly.backend.presentation.dto.dashboard.TrainingLogResponse;
import com.huly.backend.presentation.dto.dashboard.WellbeingResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/admin/chatbot")
public class ChatbotDashboardController {

    // TODO: inyectar use cases cuando se implementen los servicios reales

    @GetMapping("/emotional-categories")
    public ResponseEntity<List<EmotionalCategoryResponse>> getEmotionalCategories() {
        // TODO: reemplazar con llamada al servicio/repositorio real
        return ResponseEntity.ok(List.of(
                new EmotionalCategoryResponse("Estrés",   12, 94, "ALTA"),
                new EmotionalCategoryResponse("Ansiedad", 8,  88, "MEDIA"),
                new EmotionalCategoryResponse("Tristeza", 15, 91, "MEDIA"),
                new EmotionalCategoryResponse("Miedo",    5,  76, "BAJA"),
                new EmotionalCategoryResponse("Enojo",    9,  82, "MEDIA"),
                new EmotionalCategoryResponse("Soledad",  4,  65, "BAJA")
        ));
    }

    @GetMapping("/activities")
    public ResponseEntity<List<ActivityResponse>> getActivities() {
        // TODO: reemplazar con llamada al servicio/repositorio real
        return ResponseEntity.ok(List.of(
                new ActivityResponse("Reventar burbujas",    92),
                new ActivityResponse("Respiraciones guiadas", 85),
                new ActivityResponse("Diario emocional",      78),
                new ActivityResponse("Nubes",                 64)
        ));
    }

    @GetMapping("/wellbeing")
    public ResponseEntity<WellbeingResponse> getWellbeing() {
        // TODO: reemplazar con datos reales del historial de bienestar del usuario
        return ResponseEntity.ok(new WellbeingResponse(
                List.of(62, 58, 71, 68, 75, 65, 72),
                List.of("Lun", "Mar", "Mié", "Jue", "Vie", "Sáb", "Dom")
        ));
    }

    @GetMapping("/training-logs")
    public ResponseEntity<List<TrainingLogResponse>> getTrainingLogs() {
        // TODO: reemplazar con registros reales de conversaciones / entrenamiento del modelo
        return ResponseEntity.ok(List.of(
                new TrainingLogResponse("\"Me siento muy agobiado por el trabajo...\"", "Estrés",   98.4),
                new TrainingLogResponse("\"No sé qué hacer, mi corazón late rápido.\"", "Ansiedad", 84.2),
                new TrainingLogResponse("\"Hoy ha sido un día realmente gris.\"",        "Tristeza", 92.7)
        ));
    }
}
