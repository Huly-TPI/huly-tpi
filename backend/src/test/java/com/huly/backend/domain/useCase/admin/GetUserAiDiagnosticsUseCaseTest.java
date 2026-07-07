package com.huly.backend.domain.useCase.admin;

import com.huly.backend.domain.model.user.AppUser;
import com.huly.backend.domain.model.emotionalRecommendation.EmotionalEvent;
import com.huly.backend.domain.model.user.UserPersonalitySummary;
import com.huly.backend.domain.model.chat.ChatConversationPreference;
import com.huly.backend.domain.model.enums.CommunicationStyle;
import com.huly.backend.domain.model.enums.EmotionalEventSource;
import com.huly.backend.domain.model.enums.RecommendationDecision;
import com.huly.backend.domain.model.vector.VectorMemoryEntry;
import com.huly.backend.domain.port.VectorMemoryPort;
import com.huly.backend.domain.repository.UserPersonalitySummaryRepository;
import com.huly.backend.domain.repository.chatBotConfig.EmotionalEventRepository;
import com.huly.backend.domain.repository.user.UserRepository;
import com.huly.backend.domain.repository.chat.ChatConversationPreferenceRepository;
import com.huly.backend.domain.useCase.admin.userAiDiagnostics.GetUserAiDiagnosticsRequest;
import com.huly.backend.domain.useCase.admin.userAiDiagnostics.GetUserAiDiagnosticsResponse;
import com.huly.backend.domain.useCase.admin.userAiDiagnostics.GetUserAiDiagnosticsUseCase;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GetUserAiDiagnosticsUseCaseTest {

    private static final Long USER_ID = 1L;

    @Mock
    private UserRepository userRepository;

    @Mock
    private EmotionalEventRepository emotionalEventRepository;

    @Mock
    private VectorMemoryPort vectorMemoryPort;

    @Mock
    private UserPersonalitySummaryRepository personalitySummaryRepository;

    @Mock
    private ChatConversationPreferenceRepository preferenceRepository;

    @InjectMocks
    private GetUserAiDiagnosticsUseCase useCase;

    @Test
    @DisplayName("Lanza excepción cuando el usuario no existe")
    void executeShouldThrowWhenUserNotFound() {
        // --- arrange ---
        givenUserNotFound();

        // --- assert ---
        thenDiagnosticsThrowsUserNotFound();
    }

    @Test
    @DisplayName("Arma la respuesta separada de los objetos de negocio")
    void executeShouldReturnResponseSeparatedFromBusinessObjects() {
        // --- arrange ---
        givenUserExists();
        givenMemories(memory("mem-1", "Trabajo con mucho estres", "CHATBOT", "TEXT_MEMORY", "2026-06-14T00:00:00Z"));
        givenPersonalitySummary(personalitySummary("Perfil", null, null));
        givenEmotionalEvents(detailedAcceptedEvent());
        givenRecommendationEvents(detailedAcceptedEvent());
        givenPreference(preference("Ana", CommunicationStyle.CLOSE));

        // --- act ---
        GetUserAiDiagnosticsResponse response = diagnostics();

        // --- assert ---
        thenFullDiagnostics(response);
    }

    @Test
    @DisplayName("Usa los campos dedicados del resumen de personalidad")
    void executeShouldUseDedicatedPersonalitySummaryFields() {
        // --- arrange ---
        givenUserExists();
        givenPersonalitySummary(personalitySummary("Resumen clinico del usuario.", "Actividades de meditacion", "Eventos sociales masivos"));

        // --- act ---
        GetUserAiDiagnosticsResponse response = diagnostics();

        // --- assert ---
        thenDedicatedSummaryFields(response);
    }

    @Test
    @DisplayName("Calcula receptividad moderada cuando la mitad de las recomendaciones se aceptan")
    void executeShouldReportModerateReceptivity() {
        // --- arrange ---
        givenUserExists();
        givenRecommendationEvents(
                recommendation(1L, "respiracion guiada", RecommendationDecision.ACCEPTED),
                recommendation(2L, "diario emocional", RecommendationDecision.IGNORED));

        // --- act ---
        GetUserAiDiagnosticsResponse response = diagnostics();

        // --- assert ---
        thenModerateReceptivity(response);
    }

    @Test
    @DisplayName("Calcula baja receptividad cuando se ignoran la mayoría de las recomendaciones")
    void executeShouldReportLowReceptivity() {
        // --- arrange ---
        givenUserExists();
        givenRecommendationEvents(
                recommendation(1L, "respiracion guiada", RecommendationDecision.ACCEPTED),
                recommendation(2L, "diario emocional", RecommendationDecision.IGNORED),
                recommendation(3L, "nube", RecommendationDecision.IGNORED),
                recommendation(4L, "burbuja", RecommendationDecision.IGNORED));

        // --- act ---
        GetUserAiDiagnosticsResponse response = diagnostics();

        // --- assert ---
        thenLowReceptivity(response);
    }

    @Test
    @DisplayName("Reporta sin recomendaciones cuando no hay eventos de recomendación")
    void executeShouldReportNoRecommendations() {
        // --- arrange ---
        givenUserExists();

        // --- act ---
        GetUserAiDiagnosticsResponse response = diagnostics();

        // --- assert ---
        thenNoRecommendations(response);
    }

    @Test
    @DisplayName("Detecta temas y estrategias de afrontamiento en las memorias")
    void executeShouldDetectTopicsAndCopingStrategies() {
        // --- arrange ---
        givenUserExists();
        givenMemories(
                memory("1", "Tengo mucho examen y estudio en la universidad, me da ansiedad", "CHATBOT", "TEXT_MEMORY", null),
                memory("2", "Me gusta escuchar musica y hacer ejercicio para relajarme", "CHATBOT", "TEXT_MEMORY", null));

        // --- act ---
        GetUserAiDiagnosticsResponse response = diagnostics();

        // --- assert ---
        thenTopicsAndStrategies(response);
    }

    @Test
    @DisplayName("Limpia encabezados y markdown del resumen de personalidad")
    void executeShouldCleanPersonalitySummaryHeadersAndMarkdown() {
        // --- arrange ---
        givenUserExists();
        givenPersonalitySummary(personalitySummary("**Perfil Psicológico y Conductual** El usuario es receptivo.", "Yoga", "Pesas"));

        // --- act ---
        GetUserAiDiagnosticsResponse response = diagnostics();

        // --- assert ---
        thenSummaryEquals(response, "El usuario es receptivo.");
    }

    @Test
    @DisplayName("Limpia el encabezado con dos puntos del resumen de personalidad")
    void executeShouldCleanColonHeaderVariant() {
        // --- arrange ---
        givenUserExists();
        givenPersonalitySummary(personalitySummary("Perfil Psicológico y Conductual: seguimiento estable.", null, null));

        // --- act ---
        GetUserAiDiagnosticsResponse response = diagnostics();

        // --- assert ---
        thenSummaryEquals(response, "seguimiento estable.");
    }

    @Test
    @DisplayName("Usa el resumen por defecto cuando el resumen de personalidad es nulo")
    void executeShouldUseDefaultSummaryWhenPersonalitySummaryHasNullSummary() {
        // --- arrange ---
        givenUserExists();
        givenPersonalitySummary(personalitySummary(null, null, null));

        // --- act ---
        GetUserAiDiagnosticsResponse response = diagnostics();

        // --- assert ---
        thenSummaryEquals(response, "No tiene memorias suficientes para generar una sintesis de IA.");
    }

    @Test
    @DisplayName("Detecta decisiones de retos en la memoria vectorial")
    void executeShouldDetectChallengeDecisionsInVectorMemory() {
        // --- arrange ---
        givenUserExists();
        givenMemories(
                memory("1", "Aceptó el reto de respirar", "CHALLENGE_DECISION", "CHALLENGE_DECISION", null),
                memory("2", "Rechazó el reto de meditar", "CHALLENGE_DECISION", "CHALLENGE_DECISION", null));

        // --- act ---
        GetUserAiDiagnosticsResponse response = diagnostics();

        // --- assert ---
        thenChallengeDecisions(response);
    }

    @Test
    @DisplayName("Maneja recomendaciones en blanco y contenido de memoria nulo")
    void executeShouldHandleBlankRecommendationAndNullMemoryContent() {
        // --- arrange ---
        givenUserExists();
        givenMemories(memory("1", null, "CHATBOT", "TEXT_MEMORY", null));
        givenRecommendationEvents(recommendation(1L, "   ", null));

        // --- act ---
        GetUserAiDiagnosticsResponse response = diagnostics();

        // --- assert ---
        thenReceptivityScoreIsZero(response);
    }

    @Test
    @DisplayName("Omite la recomendación cuando el nombre generado es nulo")
    void executeShouldSkipRecommendationWithNullGeneratedName() {
        // --- arrange ---
        givenUserExists();
        givenRecommendationEvents(recommendation(1L, null, RecommendationDecision.ACCEPTED));

        // --- act ---
        GetUserAiDiagnosticsResponse response = diagnostics();

        // --- assert ---
        thenAcceptedCountedButNoActivityRecorded(response);
    }

    @Test
    @DisplayName("Maneja retos sin palabras clave y simplifica varios nombres de recomendación")
    void executeShouldHandleChallengeWithoutKeywordsAndSimplifyVariousNames() {
        // --- arrange ---
        givenUserExists();
        givenMemories(memory("1", "checking some challenge status", "CHALLENGE_DECISION", "CHALLENGE_DECISION", null));
        givenRecommendationEvents(
                recommendation(1L, "el reto semanal es correr", RecommendationDecision.ACCEPTED),
                recommendation(2L, "some unrecognized activity", RecommendationDecision.ACCEPTED));

        // --- act ---
        GetUserAiDiagnosticsResponse response = diagnostics();

        // --- assert ---
        thenAcceptedContains(response, "Retos Diarios", "some unrecognized activity");
    }

    @Test
    @DisplayName("Simplifica todos los sinónimos de recomendación (español e inglés)")
    void executeShouldSimplifyAllRecommendationSynonyms() {
        // --- arrange ---
        givenUserExists();
        givenRecommendationEvents(
                recommendation(1L, "necesito respiración profunda", RecommendationDecision.ACCEPTED),
                recommendation(2L, "escribe en tu journal", RecommendationDecision.ACCEPTED),
                recommendation(3L, "mira las cloud animations", RecommendationDecision.ACCEPTED),
                recommendation(4L, "juega bubble game", RecommendationDecision.IGNORED),
                recommendation(5L, "weekly challenge", RecommendationDecision.IGNORED),
                recommendation(6L, "try breathing now", RecommendationDecision.IGNORED));

        // --- act ---
        GetUserAiDiagnosticsResponse response = diagnostics();

        // --- assert ---
        thenSynonymsSimplified(response);
    }

    @Test
    @DisplayName("Reconoce las variantes de frases de aceptación y rechazo y el match por tipo de contenido")
    void executeShouldRecognizeDecisionPhraseVariantsAndContentTypeMatch() {
        // --- arrange ---
        givenUserExists();
        givenMemories(
                memory("1", "reto aceptado hoy", "CHALLENGE_DECISION", "CHALLENGE_DECISION", null),
                memory("2", "acepto el reto de hoy", "CHATBOT", "CHALLENGE_DECISION", null),
                memory("3", "reto rechazado ayer", "CHALLENGE_DECISION", "CHALLENGE_DECISION", null),
                memory("4", "rechazo el reto ahora", "CHALLENGE_DECISION", "CHALLENGE_DECISION", null));

        // --- act ---
        GetUserAiDiagnosticsResponse response = diagnostics();

        // --- assert ---
        thenChallengeDecisions(response);
    }

    @Test
    @DisplayName("Omite eventos sin emoción detectada y elige la emoción dominante por frecuencia")
    void executeShouldSkipEventsWithoutEmotionAndPickDominantEmotion() {
        // --- arrange ---
        givenUserExists();
        givenEmotionalEvents(
                emotionalEventWithEmotion(null),
                emotionalEventWithEmotion("ALEGRIA"),
                emotionalEventWithEmotion("ALEGRIA"),
                emotionalEventWithEmotion("TRISTEZA"));

        // --- act ---
        GetUserAiDiagnosticsResponse response = diagnostics();

        // --- assert ---
        thenDominantEmotionIs(response, "ALEGRIA");
    }

    @Test
    @DisplayName("Ignora los campos dedicados en blanco del resumen de personalidad")
    void executeShouldIgnoreBlankDedicatedPersonalitySummaryFields() {
        // --- arrange ---
        givenUserExists();
        givenPersonalitySummary(personalitySummary("Resumen valido.", "   ", "  "));

        // --- act ---
        GetUserAiDiagnosticsResponse response = diagnostics();

        // --- assert ---
        thenSummaryKeptWithoutDedicatedActivities(response);
    }

    // La rama `if (finalSummary != null)` previa a la limpieza de encabezados es código defensivo
    // inalcanzable: finalSummary siempre se asigna no nulo (el resumen de personalidad o el texto por
    // defecto), por lo que su rama falsa no puede ejercitarse.

    // --- arrange ---

    private void givenUserExists() {
        when(userRepository.findById(USER_ID)).thenReturn(Optional.of(AppUser.builder().id(USER_ID).build()));
    }

    private void givenUserNotFound() {
        when(userRepository.findById(USER_ID)).thenReturn(Optional.empty());
    }

    private void givenMemories(VectorMemoryEntry... memories) {
        when(vectorMemoryPort.findMemoriesByUserIdExcludingSummary(USER_ID)).thenReturn(List.of(memories));
    }

    private void givenEmotionalEvents(EmotionalEvent... events) {
        when(emotionalEventRepository.findByUserId(USER_ID)).thenReturn(List.of(events));
    }

    private void givenRecommendationEvents(EmotionalEvent... events) {
        when(emotionalEventRepository.findRecommendationEventsByUserId(USER_ID)).thenReturn(List.of(events));
    }

    private void givenPersonalitySummary(UserPersonalitySummary summary) {
        when(personalitySummaryRepository.findByUserId(USER_ID)).thenReturn(Optional.of(summary));
    }

    private void givenPreference(ChatConversationPreference preference) {
        when(preferenceRepository.findByUserId(USER_ID)).thenReturn(Optional.of(preference));
    }

    private EmotionalEvent detailedAcceptedEvent() {
        return EmotionalEvent.builder()
                .id(20L)
                .userId(USER_ID)
                .source(EmotionalEventSource.CHATBOT)
                .inputText("Me siento cansado")
                .detectedEmotion("cansado")
                .confidence(0.9)
                .generatedRecommendation("Respiracion guiada")
                .recommendationDecision(RecommendationDecision.ACCEPTED)
                .createdAt(Instant.now())
                .build();
    }

    private EmotionalEvent emotionalEventWithEmotion(String detectedEmotion) {
        return EmotionalEvent.builder()
                .id(1L)
                .userId(USER_ID)
                .source(EmotionalEventSource.CHATBOT)
                .detectedEmotion(detectedEmotion)
                .createdAt(Instant.now())
                .build();
    }

    private EmotionalEvent recommendation(Long id, String generatedRecommendation, RecommendationDecision decision) {
        return EmotionalEvent.builder()
                .id(id)
                .userId(USER_ID)
                .source(EmotionalEventSource.CHATBOT)
                .generatedRecommendation(generatedRecommendation)
                .recommendationDecision(decision)
                .build();
    }

    private VectorMemoryEntry memory(String id, String content, String sourceType, String contentType, String createdAt) {
        return new VectorMemoryEntry(id, content, sourceType, contentType, createdAt);
    }

    private UserPersonalitySummary personalitySummary(String summary, String accepted, String rejected) {
        return UserPersonalitySummary.builder()
                .userId(USER_ID)
                .summary(summary)
                .accepted(accepted)
                .rejected(rejected)
                .generatedAt(Instant.now())
                .updatedAt(Instant.now())
                .build();
    }

    private ChatConversationPreference preference(String preferredName, CommunicationStyle style) {
        return ChatConversationPreference.builder()
                .preferredName(preferredName)
                .communicationStyle(style)
                .build();
    }

    // --- act ---

    private GetUserAiDiagnosticsResponse diagnostics() {
        return useCase.execute(new GetUserAiDiagnosticsRequest(USER_ID));
    }

    // --- assert ---

    private void thenDiagnosticsThrowsUserNotFound() {
        assertThatThrownBy(this::diagnostics)
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Usuario no encontrado");
    }

    private void thenFullDiagnostics(GetUserAiDiagnosticsResponse response) {
        assertThat(response.aiMemories()).singleElement().satisfies(item -> {
            assertThat(item.id()).isEqualTo("mem-1");
            assertThat(item.content()).isEqualTo("Trabajo con mucho estres");
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

    private void thenDedicatedSummaryFields(GetUserAiDiagnosticsResponse response) {
        assertThat(response.personalitySummary()).isEqualTo("Resumen clinico del usuario.");
        assertThat(response.acceptedActivities()).containsExactly("Actividades de meditacion");
        assertThat(response.ignoredActivities()).containsExactly("Eventos sociales masivos");
    }

    private void thenModerateReceptivity(GetUserAiDiagnosticsResponse response) {
        assertThat(response.receptivityScore()).isEqualTo(50);
        assertThat(response.receptivityLabel()).isEqualTo("Receptividad moderada");
        assertThat(response.acceptedActivities()).contains("Respiración Guiada");
        assertThat(response.ignoredActivities()).contains("Diario Emocional");
    }

    private void thenLowReceptivity(GetUserAiDiagnosticsResponse response) {
        assertThat(response.receptivityScore()).isEqualTo(25);
        assertThat(response.receptivityLabel()).isEqualTo("Baja receptividad");
    }

    private void thenNoRecommendations(GetUserAiDiagnosticsResponse response) {
        assertThat(response.receptivityScore()).isZero();
        assertThat(response.receptivityLabel()).isEqualTo("Sin recomendaciones registradas");
    }

    private void thenTopicsAndStrategies(GetUserAiDiagnosticsResponse response) {
        assertThat(response.topicsDetected()).contains("Estrés laboral o académico", "Ansiedad o Preocupaciones");
        assertThat(response.copingStrategies()).contains("Música y Arte", "Actividad Física", "Meditación y Respiración");
    }

    private void thenSummaryEquals(GetUserAiDiagnosticsResponse response, String expectedSummary) {
        assertThat(response.personalitySummary()).isEqualTo(expectedSummary);
    }

    private void thenChallengeDecisions(GetUserAiDiagnosticsResponse response) {
        assertThat(response.receptivityScore()).isEqualTo(50);
        assertThat(response.acceptedActivities()).contains("Retos Diarios");
        assertThat(response.ignoredActivities()).contains("Retos Diarios");
    }

    private void thenDominantEmotionIs(GetUserAiDiagnosticsResponse response, String expectedEmotion) {
        assertThat(response.dominantEmotion()).isEqualTo(expectedEmotion);
        assertThat(response.emotionDistribution()).containsEntry("ALEGRIA", 2).containsEntry("TRISTEZA", 1);
    }

    private void thenSummaryKeptWithoutDedicatedActivities(GetUserAiDiagnosticsResponse response) {
        assertThat(response.personalitySummary()).isEqualTo("Resumen valido.");
        assertThat(response.acceptedActivities()).isEmpty();
        assertThat(response.ignoredActivities()).isEmpty();
    }

    private void thenReceptivityScoreIsZero(GetUserAiDiagnosticsResponse response) {
        assertThat(response.receptivityScore()).isZero();
    }

    private void thenAcceptedCountedButNoActivityRecorded(GetUserAiDiagnosticsResponse response) {
        assertThat(response.receptivityScore()).isEqualTo(100);
        assertThat(response.acceptedActivities()).isEmpty();
    }

    private void thenAcceptedContains(GetUserAiDiagnosticsResponse response, String... expected) {
        assertThat(response.acceptedActivities()).contains(expected);
    }

    private void thenSynonymsSimplified(GetUserAiDiagnosticsResponse response) {
        assertThat(response.acceptedActivities())
                .contains("Respiración Guiada", "Diario Emocional", "Nubes de Pensamiento");
        assertThat(response.ignoredActivities())
                .contains("Reventar Burbujas", "Retos Diarios");
    }
}
