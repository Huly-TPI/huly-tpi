package com.huly.backend.domain.useCase.admin.userAiDiagnostics;

import com.huly.backend.domain.model.AppUser;
import com.huly.backend.domain.model.EmotionalEvent;
import com.huly.backend.domain.model.chat.ChatConversationPreference;
import com.huly.backend.domain.model.enums.CommunicationStyle;
import com.huly.backend.domain.model.enums.EmotionalEventSource;
import com.huly.backend.domain.model.enums.RecommendationDecision;
import com.huly.backend.domain.model.vector.VectorMemoryEntry;
import com.huly.backend.domain.repository.EmotionalEventRepository;
import com.huly.backend.domain.repository.UserRepository;
import com.huly.backend.domain.repository.VectorMemoryRepository;
import com.huly.backend.domain.repository.chat.ChatConversationPreferenceRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class GetUserAiDiagnosticsUseCaseTest {

    private static final Long USER_ID = 1L;

    private UserRepository userRepository;
    private EmotionalEventRepository emotionalEventRepository;
    private VectorMemoryRepository vectorMemoryRepository;
    private ChatConversationPreferenceRepository preferenceRepository;
    private GetUserAiDiagnosticsUseCase useCase;

    @BeforeEach
    void setUp() {
        userRepository = mock(UserRepository.class);
        emotionalEventRepository = mock(EmotionalEventRepository.class);
        vectorMemoryRepository = mock(VectorMemoryRepository.class);
        preferenceRepository = mock(ChatConversationPreferenceRepository.class);
        useCase = new GetUserAiDiagnosticsUseCase(
                userRepository,
                emotionalEventRepository,
                vectorMemoryRepository,
                preferenceRepository
        );
    }

    @Test
    void execute_shouldThrowException_whenUserNotFound() {
        when(userRepository.findById(USER_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> useCase.execute(new GetUserAiDiagnosticsRequest(USER_ID)))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Usuario no encontrado");
    }

    @Test
    void execute_shouldReturnUseCaseResponseSeparatedFromBusinessObjects() {
        EmotionalEvent event = EmotionalEvent.builder()
                .id(20L)
                .userId(USER_ID)
                .source(EmotionalEventSource.CHATBOT)
                .inputText("Me siento cansado")
                .detectedEmotion("cansado")
                .confidence(0.9)
                .generatedRecommendation("Respiración guiada")
                .recommendationDecision(RecommendationDecision.ACCEPTED)
                .createdAt(Instant.now())
                .build();
        VectorMemoryEntry memory = new VectorMemoryEntry(
                "mem-1",
                "Trabajo con mucho estrés",
                "CHATBOT",
                "TEXT_MEMORY",
                "2026-06-14T00:00:00Z"
        );
        ChatConversationPreference preference = ChatConversationPreference.builder()
                .preferredName("Ana")
                .communicationStyle(CommunicationStyle.CLOSE)
                .build();

        when(userRepository.findById(USER_ID)).thenReturn(Optional.of(AppUser.builder().id(USER_ID).build()));
        when(vectorMemoryRepository.findMemoriesByUserIdExcludingSummary(USER_ID)).thenReturn(List.of(memory));
        when(vectorMemoryRepository.findPersonalitySummaryByUserId(USER_ID)).thenReturn(Optional.of("Perfil"));
        when(emotionalEventRepository.findByUserId(USER_ID)).thenReturn(List.of(event));
        when(emotionalEventRepository.findRecommendationEventsByUserId(USER_ID)).thenReturn(List.of(event));
        when(preferenceRepository.findByUserId(USER_ID)).thenReturn(Optional.of(preference));

        GetUserAiDiagnosticsResponse response = useCase.execute(new GetUserAiDiagnosticsRequest(USER_ID));

        assertThat(response.aiMemories()).singleElement().satisfies(item -> {
            assertThat(item.id()).isEqualTo("mem-1");
            assertThat(item.content()).isEqualTo("Trabajo con mucho estrés");
        });
        assertThat(response.emotionalEvents()).singleElement().satisfies(item -> {
            assertThat(item.detectedEmotion()).isEqualTo("cansado");
            assertThat(item.recommendationDecision()).isEqualTo("ACCEPTED");
        });
        assertThat(response.preferredName()).isEqualTo("Ana");
        assertThat(response.communicationStyle()).isEqualTo("cercano");
        assertThat(response.personalitySummary()).isEqualTo("Perfil");
        assertThat(response.receptivityScore()).isEqualTo(100);
        assertThat(response.receptivityLabel()).isEqualTo("Alta receptividad");
    }

    @Test
    void execute_shouldParseJsonPersonalitySummaryAndPopulateReceptivityFields() {
        String jsonSummary = """
                {
                  "summary": "Resumen clinico del usuario.",
                  "accepted": "Actividades de meditacion",
                  "rejected": "Eventos sociales masivos"
                }
                """;

        when(userRepository.findById(USER_ID)).thenReturn(Optional.of(AppUser.builder().id(USER_ID).build()));
        when(vectorMemoryRepository.findMemoriesByUserIdExcludingSummary(USER_ID)).thenReturn(List.of());
        when(vectorMemoryRepository.findPersonalitySummaryByUserId(USER_ID)).thenReturn(Optional.of(jsonSummary));
        when(emotionalEventRepository.findByUserId(USER_ID)).thenReturn(List.of());
        when(emotionalEventRepository.findRecommendationEventsByUserId(USER_ID)).thenReturn(List.of());
        when(preferenceRepository.findByUserId(USER_ID)).thenReturn(Optional.empty());

        GetUserAiDiagnosticsResponse response = useCase.execute(new GetUserAiDiagnosticsRequest(USER_ID));

        assertThat(response.personalitySummary()).isEqualTo("Resumen clinico del usuario.");
        assertThat(response.acceptedActivities()).containsExactly("Actividades de meditacion");
        assertThat(response.ignoredActivities()).containsExactly("Eventos sociales masivos");
    }

    @Test
    void execute_shouldHandleDifferentReceptivityLabelsAndScores() {
        EmotionalEvent ev1 = EmotionalEvent.builder()
                .id(1L).userId(USER_ID).source(EmotionalEventSource.CHATBOT)
                .generatedRecommendation("respiración guiada")
                .recommendationDecision(RecommendationDecision.ACCEPTED).build();
        EmotionalEvent ev2 = EmotionalEvent.builder()
                .id(2L).userId(USER_ID).source(EmotionalEventSource.CHATBOT)
                .generatedRecommendation("diario emocional")
                .recommendationDecision(RecommendationDecision.IGNORED).build();

        when(userRepository.findById(USER_ID)).thenReturn(Optional.of(AppUser.builder().id(USER_ID).build()));
        when(vectorMemoryRepository.findMemoriesByUserIdExcludingSummary(USER_ID)).thenReturn(List.of());
        when(vectorMemoryRepository.findPersonalitySummaryByUserId(USER_ID)).thenReturn(Optional.empty());
        when(emotionalEventRepository.findByUserId(USER_ID)).thenReturn(List.of(ev1, ev2));
        when(emotionalEventRepository.findRecommendationEventsByUserId(USER_ID)).thenReturn(List.of(ev1, ev2));
        when(preferenceRepository.findByUserId(USER_ID)).thenReturn(Optional.empty());

        GetUserAiDiagnosticsResponse responseMod = useCase.execute(new GetUserAiDiagnosticsRequest(USER_ID));
        assertThat(responseMod.receptivityScore()).isEqualTo(50);
        assertThat(responseMod.receptivityLabel()).isEqualTo("Receptividad moderada");
        assertThat(responseMod.acceptedActivities()).contains("Respiración Guiada");
        assertThat(responseMod.ignoredActivities()).contains("Diario Emocional");

        EmotionalEvent ev3 = EmotionalEvent.builder()
                .id(3L).userId(USER_ID).source(EmotionalEventSource.CHATBOT)
                .generatedRecommendation("nube")
                .recommendationDecision(RecommendationDecision.IGNORED).build();
        EmotionalEvent ev4 = EmotionalEvent.builder()
                .id(4L).userId(USER_ID).source(EmotionalEventSource.CHATBOT)
                .generatedRecommendation("burbuja")
                .recommendationDecision(RecommendationDecision.IGNORED).build();
        when(emotionalEventRepository.findByUserId(USER_ID)).thenReturn(List.of(ev1, ev2, ev3, ev4));
        when(emotionalEventRepository.findRecommendationEventsByUserId(USER_ID)).thenReturn(List.of(ev1, ev2, ev3, ev4));

        GetUserAiDiagnosticsResponse responseLow = useCase.execute(new GetUserAiDiagnosticsRequest(USER_ID));
        assertThat(responseLow.receptivityScore()).isEqualTo(25);
        assertThat(responseLow.receptivityLabel()).isEqualTo("Baja receptividad");

        when(emotionalEventRepository.findByUserId(USER_ID)).thenReturn(List.of());
        when(emotionalEventRepository.findRecommendationEventsByUserId(USER_ID)).thenReturn(List.of());

        GetUserAiDiagnosticsResponse responseNone = useCase.execute(new GetUserAiDiagnosticsRequest(USER_ID));
        assertThat(responseNone.receptivityScore()).isZero();
        assertThat(responseNone.receptivityLabel()).isEqualTo("Sin recomendaciones registradas");
    }

    @Test
    void execute_shouldDetectTopicsAndCopingStrategies() {
        VectorMemoryEntry memory1 = new VectorMemoryEntry("1", "Tengo mucho examen y estudio en la universidad, me da ansiedad", "CHATBOT", "TEXT_MEMORY", null);
        VectorMemoryEntry memory2 = new VectorMemoryEntry("2", "Me gusta escuchar música y hacer ejercicio para relajarme", "CHATBOT", "TEXT_MEMORY", null);

        when(userRepository.findById(USER_ID)).thenReturn(Optional.of(AppUser.builder().id(USER_ID).build()));
        when(vectorMemoryRepository.findMemoriesByUserIdExcludingSummary(USER_ID)).thenReturn(List.of(memory1, memory2));
        when(vectorMemoryRepository.findPersonalitySummaryByUserId(USER_ID)).thenReturn(Optional.empty());
        when(emotionalEventRepository.findByUserId(USER_ID)).thenReturn(List.of());
        when(emotionalEventRepository.findRecommendationEventsByUserId(USER_ID)).thenReturn(List.of());
        when(preferenceRepository.findByUserId(USER_ID)).thenReturn(Optional.empty());

        GetUserAiDiagnosticsResponse response = useCase.execute(new GetUserAiDiagnosticsRequest(USER_ID));
        assertThat(response.topicsDetected()).contains("Estrés laboral o académico", "Ansiedad o Preocupaciones");
        assertThat(response.copingStrategies()).contains("Música y Arte", "Actividad Física", "Meditación y Respiración");
    }

    @Test
    void execute_shouldCleanPersonalitySummaryHeadersAndMarkdown() {
        String markdownSummary = """
                ```json
                {
                  "summary": "**Perfil Psicológico y Conductual** El usuario es receptivo.",
                  "accepted": "Yoga",
                  "rejected": "Pesas"
                }
                ```
                """;

        when(userRepository.findById(USER_ID)).thenReturn(Optional.of(AppUser.builder().id(USER_ID).build()));
        when(vectorMemoryRepository.findMemoriesByUserIdExcludingSummary(USER_ID)).thenReturn(List.of());
        when(vectorMemoryRepository.findPersonalitySummaryByUserId(USER_ID)).thenReturn(Optional.of(markdownSummary));
        when(emotionalEventRepository.findByUserId(USER_ID)).thenReturn(List.of());
        when(emotionalEventRepository.findRecommendationEventsByUserId(USER_ID)).thenReturn(List.of());
        when(preferenceRepository.findByUserId(USER_ID)).thenReturn(Optional.empty());

        GetUserAiDiagnosticsResponse response = useCase.execute(new GetUserAiDiagnosticsRequest(USER_ID));
        assertThat(response.personalitySummary()).isEqualTo("El usuario es receptivo.");
    }

    @Test
    void execute_shouldDetectChallengeDecisionsInVectorMemory() {
        VectorMemoryEntry memory1 = new VectorMemoryEntry("1", "Aceptó el reto de respirar", "CHALLENGE_DECISION", "CHALLENGE_DECISION", null);
        VectorMemoryEntry memory2 = new VectorMemoryEntry("2", "Rechazó el reto de meditar", "CHALLENGE_DECISION", "CHALLENGE_DECISION", null);

        when(userRepository.findById(USER_ID)).thenReturn(Optional.of(AppUser.builder().id(USER_ID).build()));
        when(vectorMemoryRepository.findMemoriesByUserIdExcludingSummary(USER_ID)).thenReturn(List.of(memory1, memory2));
        when(vectorMemoryRepository.findPersonalitySummaryByUserId(USER_ID)).thenReturn(Optional.empty());
        when(emotionalEventRepository.findByUserId(USER_ID)).thenReturn(List.of());
        when(emotionalEventRepository.findRecommendationEventsByUserId(USER_ID)).thenReturn(List.of());
        when(preferenceRepository.findByUserId(USER_ID)).thenReturn(Optional.empty());

        GetUserAiDiagnosticsResponse response = useCase.execute(new GetUserAiDiagnosticsRequest(USER_ID));
        assertThat(response.receptivityScore()).isEqualTo(50);
        assertThat(response.acceptedActivities()).contains("Retos Diarios");
        assertThat(response.ignoredActivities()).contains("Retos Diarios");
    }

    @Test
    void execute_shouldHandleJsonParsingErrors() {
        String invalidJson = "{invalidJsonString";
        when(userRepository.findById(USER_ID)).thenReturn(Optional.of(AppUser.builder().id(USER_ID).build()));
        when(vectorMemoryRepository.findMemoriesByUserIdExcludingSummary(USER_ID)).thenReturn(List.of());
        when(vectorMemoryRepository.findPersonalitySummaryByUserId(USER_ID)).thenReturn(Optional.of(invalidJson));
        when(emotionalEventRepository.findByUserId(USER_ID)).thenReturn(List.of());
        when(emotionalEventRepository.findRecommendationEventsByUserId(USER_ID)).thenReturn(List.of());
        when(preferenceRepository.findByUserId(USER_ID)).thenReturn(Optional.empty());

        GetUserAiDiagnosticsResponse response = useCase.execute(new GetUserAiDiagnosticsRequest(USER_ID));
        assertThat(response.personalitySummary()).isEqualTo(invalidJson);
    }

    @Test
    void execute_shouldHandleMissingGeneratedRecommendationAndNullMemoryContent() {
        EmotionalEvent blankRecEvent = EmotionalEvent.builder()
                .id(1L).userId(USER_ID).source(EmotionalEventSource.CHATBOT)
                .generatedRecommendation("   ").build();
        VectorMemoryEntry nullContentMemory = new VectorMemoryEntry("1", null, "CHATBOT", "TEXT_MEMORY", null);

        when(userRepository.findById(USER_ID)).thenReturn(Optional.of(AppUser.builder().id(USER_ID).build()));
        when(vectorMemoryRepository.findMemoriesByUserIdExcludingSummary(USER_ID)).thenReturn(List.of(nullContentMemory));
        when(vectorMemoryRepository.findPersonalitySummaryByUserId(USER_ID)).thenReturn(Optional.empty());
        when(emotionalEventRepository.findByUserId(USER_ID)).thenReturn(List.of(blankRecEvent));
        when(emotionalEventRepository.findRecommendationEventsByUserId(USER_ID)).thenReturn(List.of(blankRecEvent));
        when(preferenceRepository.findByUserId(USER_ID)).thenReturn(Optional.empty());

        GetUserAiDiagnosticsResponse response = useCase.execute(new GetUserAiDiagnosticsRequest(USER_ID));
        assertThat(response.receptivityScore()).isZero();
    }

    @Test
    void execute_shouldHandleChallengeDecisionWithoutKeywordsAndSimplifyVariousRecommendationNames() {
        VectorMemoryEntry irrelevantMemory = new VectorMemoryEntry("1", "checking some challenge status", "CHALLENGE_DECISION", "CHALLENGE_DECISION", null);
        EmotionalEvent challengeRecEvent = EmotionalEvent.builder()
                .id(1L).userId(USER_ID).source(EmotionalEventSource.CHATBOT)
                .generatedRecommendation("el reto semanal es correr")
                .recommendationDecision(RecommendationDecision.ACCEPTED).build();
        EmotionalEvent otherRecEvent = EmotionalEvent.builder()
                .id(2L).userId(USER_ID).source(EmotionalEventSource.CHATBOT)
                .generatedRecommendation("some unrecognized activity")
                .recommendationDecision(RecommendationDecision.ACCEPTED).build();

        when(userRepository.findById(USER_ID)).thenReturn(Optional.of(AppUser.builder().id(USER_ID).build()));
        when(vectorMemoryRepository.findMemoriesByUserIdExcludingSummary(USER_ID)).thenReturn(List.of(irrelevantMemory));
        when(vectorMemoryRepository.findPersonalitySummaryByUserId(USER_ID)).thenReturn(Optional.empty());
        when(emotionalEventRepository.findByUserId(USER_ID)).thenReturn(List.of(challengeRecEvent, otherRecEvent));
        when(emotionalEventRepository.findRecommendationEventsByUserId(USER_ID)).thenReturn(List.of(challengeRecEvent, otherRecEvent));
        when(preferenceRepository.findByUserId(USER_ID)).thenReturn(Optional.empty());

        GetUserAiDiagnosticsResponse response = useCase.execute(new GetUserAiDiagnosticsRequest(USER_ID));
        assertThat(response.acceptedActivities()).contains("Retos Diarios", "some unrecognized activity");
    }
}
