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
}
