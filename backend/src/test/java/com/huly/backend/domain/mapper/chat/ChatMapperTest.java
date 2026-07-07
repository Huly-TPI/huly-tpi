package com.huly.backend.domain.mapper.chat;

import com.huly.backend.domain.dto.chat.ChatHistoryResponse;
import com.huly.backend.domain.dto.chat.ChatReplyResponse;
import com.huly.backend.domain.dto.emotionalEvent.CreateEmotionalEventRequest;
import com.huly.backend.domain.dto.emotionalEvent.EmotionalEventResponse;
import com.huly.backend.domain.dto.emotionalRecommendation.EmotionalRecommendationItem;
import com.huly.backend.domain.model.chat.ChatMessage;
import com.huly.backend.domain.model.chat.ChatReply;
import com.huly.backend.domain.model.chat.EmotionalAnalysisResult;
import com.huly.backend.domain.model.chat.SuggestedChatAction;
import com.huly.backend.domain.model.enums.ActivityType;
import com.huly.backend.domain.model.enums.EmotionalEventSource;
import com.huly.backend.domain.model.enums.EmotionType;
import com.huly.backend.domain.model.enums.MessageRole;
import com.huly.backend.domain.model.enums.RecommendationDecision;
import com.huly.backend.domain.model.vector.SaveVectorMemoryCommand;
import com.huly.backend.domain.model.vector.VectorMemorySource;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;

class ChatMapperTest {

    private final ChatMapper mapper = new ChatMapper();

    // Estado de arrange para los tests fragmentados nuevos (una instancia por @Test, así que se resetea solo).
    private Long userId;
    private String conversationId;
    private String message;
    private ChatReply.GeneratedChallenge generatedChallenge;
    private Long emotionalEventId;
    private SuggestedChatAction suggestedAction;
    private EmotionalAnalysisResult analysisResult;
    private EmotionalRecommendationItem recommendationItem;

    @Test
    @DisplayName("Arma el request de evento emocional del chatbot con los datos del análisis")
    void toCreateEmotionalEventRequestShouldMapAnalysisAndRecommendation() {
        EmotionalAnalysisResult analysis = analysis(EmotionType.GRIEF, 0.92, 0.88, "acompanarse");
        EmotionalRecommendationItem recommendation = recommendation(
                7L, "Diario emocional", "Un espacio para ordenar", "Recomendada");

        CreateEmotionalEventRequest request = mapper.toCreateEmotionalEventRequest(
                "me siento mal", 3L, analysis, recommendation);

        assertThat(request.source()).isEqualTo(EmotionalEventSource.CHATBOT);
        assertThat(request.inputText()).isEqualTo("me siento mal");
        assertThat(request.detectedEmotion()).isEqualTo("GRIEF");
        assertThat(request.confidence()).isEqualTo(0.92);
        assertThat(request.intensity()).isEqualTo(0.88);
        assertThat(request.userGoal()).isEqualTo("acompanarse");
        assertThat(request.recommendedActivityId()).isEqualTo(7L);
        assertThat(request.chosenActivityId()).isNull();
        assertThat(request.generatedRecommendation())
                .isEqualTo("Diario emocional: Un espacio para ordenar Recomendada");
    }

    @Test
    @DisplayName("Usa NEUTRAL como emoción por defecto cuando el análisis no detecta ninguna")
    void toCreateEmotionalEventRequestShouldDefaultToNeutralEmotion() {
        EmotionalAnalysisResult analysis = analysis(null, 0.5, 0.2, null);
        EmotionalRecommendationItem recommendation = recommendation(
                1L, "Respiración", "Práctica breve", "Ayuda a regular");

        CreateEmotionalEventRequest request = mapper.toCreateEmotionalEventRequest(
                "hola", 1L, analysis, recommendation);

        assertThat(request.detectedEmotion()).isEqualTo("NEUTRAL");
    }

    @Test
    @DisplayName("Omite la razón cuando viene vacía al armar el texto de la recomendación")
    void toCreateEmotionalEventRequestShouldOmitBlankReason() {
        EmotionalAnalysisResult analysis = analysis(EmotionType.STRESS, 0.9, 0.7, "calmarse");
        EmotionalRecommendationItem recommendation = recommendation(
                2L, "Respiración", "Práctica breve", null);

        CreateEmotionalEventRequest request = mapper.toCreateEmotionalEventRequest(
                "estresado", 1L, analysis, recommendation);

        assertThat(request.generatedRecommendation()).isEqualTo("Respiración: Práctica breve");
    }

    @Test
    @DisplayName("Arma la acción sugerida con la URL de actividades y el id del evento")
    void toSuggestedActionShouldMapRecommendationAndEvent() {
        EmotionalRecommendationItem recommendation = recommendation(
                7L, "Diario emocional", "Un espacio para ordenar", "Recomendada");
        EmotionalEventResponse event = eventResponse(50L);

        SuggestedChatAction action = mapper.toSuggestedAction(recommendation, event);

        assertThat(action.type()).isEqualTo(ActivityType.DIARY);
        assertThat(action.activityId()).isEqualTo(7L);
        assertThat(action.title()).isEqualTo("Diario emocional");
        assertThat(action.description()).isEqualTo("Un espacio para ordenar");
        assertThat(action.actionUrl()).isEqualTo("/diary");
        assertThat(action.emotionalEventId()).isEqualTo(50L);
    }

    @Test
    @DisplayName("Convierte la ChatReply de dominio en su DTO de respuesta")
    void toChatReplyResponseShouldMirrorReplyData() {
        SuggestedChatAction action = new SuggestedChatAction(
                ActivityType.DIARY, 7L, "Diario", "desc", "/api/activities", 50L);
        ChatReply.GeneratedChallenge challenge = new ChatReply.GeneratedChallenge("Reto", "Hacé algo");
        ChatReply reply = new ChatReply("hola", EmotionType.JOY, 8, true, "palabra", action, challenge);

        ChatReplyResponse response = mapper.toChatReplyResponse(reply);

        assertThat(response.content()).isEqualTo("hola");
        assertThat(response.detectedEmotion()).isEqualTo(EmotionType.JOY);
        assertThat(response.intensity()).isEqualTo(8);
        assertThat(response.riskDetected()).isTrue();
        assertThat(response.matchedWord()).isEqualTo("palabra");
        assertThat(response.suggestedAction().type()).isEqualTo(ActivityType.DIARY);
        assertThat(response.suggestedAction().activityId()).isEqualTo(7L);
        assertThat(response.suggestedAction().emotionalEventId()).isEqualTo(50L);
        assertThat(response.generatedChallenge().title()).isEqualTo("Reto");
        assertThat(response.generatedChallenge().description()).isEqualTo("Hacé algo");
    }

    @Test
    @DisplayName("Convierte el ChatMessage de dominio en el mensaje del historial")
    void toHistoryMessageShouldMirrorMessageData() {
        Instant now = Instant.now();
        ChatMessage message = new ChatMessage(
                3L, MessageRole.ASSISTANT, "resp", false, EmotionType.CALM, now,
                null, null, "ACCEPTED", "REJECTED");

        ChatHistoryResponse.Message result = mapper.toHistoryMessage(message);

        assertThat(result.id()).isEqualTo(3L);
        assertThat(result.role()).isEqualTo(MessageRole.ASSISTANT);
        assertThat(result.content()).isEqualTo("resp");
        assertThat(result.detectedEmotion()).isEqualTo(EmotionType.CALM);
        assertThat(result.createdAt()).isEqualTo(now);
        assertThat(result.suggestedActionDecision()).isEqualTo("ACCEPTED");
        assertThat(result.challengeDecision()).isEqualTo("REJECTED");
    }

    @Test
    @DisplayName("Arma la memoria del mensaje del usuario usando su id como sourceId")
    void toUserMemoryCommandShouldMapUserIdAsSourceId() {
        // --- arrange ---
        givenUserId(3L);
        givenConversation("conv-1");
        givenMessage("hola huly");

        // --- act ---
        SaveVectorMemoryCommand command = mapUserMemory();

        // --- assert ---
        thenUserMemoryMapped(command, "3");
    }

    @Test
    @DisplayName("Deja el sourceId nulo al armar la memoria del mensaje cuando no hay usuario")
    void toUserMemoryCommandShouldLeaveNullSourceIdWhenUserIdIsNull() {
        // --- arrange ---
        givenNoUser();
        givenConversation("conv-1");
        givenMessage("hola");

        // --- act ---
        SaveVectorMemoryCommand command = mapUserMemory();

        // --- assert ---
        thenUserMemoryMapped(command, null);
    }

    @Test
    @DisplayName("Arma el comando del reto generado con la conversación y la descripción presentes")
    void toGeneratedChallengeCommandShouldMapChallengeWithConversationAndDescription() {
        // --- arrange ---
        givenUserId(5L);
        givenConversation("conv-9");
        givenChallenge("Reto matinal", "Correr");

        // --- act ---
        SaveVectorMemoryCommand command = mapGeneratedChallenge();

        // --- assert ---
        thenChallengeMapped(command,
                "generated-challenge:conv-9:Reto matinal",
                "Huly sugirio el reto: Reto matinal. Descripcion: Correr.",
                "Reto matinal",
                "Correr");
    }

    @Test
    @DisplayName("Usa 'unknown' y descripción vacía cuando la conversación y la descripción son nulas")
    void toGeneratedChallengeCommandShouldUseUnknownAndEmptyDescriptionWhenConversationAndDescriptionAreNull() {
        // --- arrange ---
        givenUserId(5L);
        givenNoConversation();
        givenChallenge("Reto", null);

        // --- act ---
        SaveVectorMemoryCommand command = mapGeneratedChallenge();

        // --- assert ---
        thenChallengeMapped(command,
                "generated-challenge:unknown:Reto",
                "Huly sugirio el reto: Reto. Descripcion: .",
                "Reto",
                "");
    }

    @Test
    @DisplayName("Usa 'unknown' como conversación en el sourceId cuando el id de conversación viene en blanco")
    void toGeneratedChallengeCommandShouldUseUnknownWhenConversationIsBlank() {
        // --- arrange ---
        givenUserId(5L);
        givenConversation("   ");
        givenChallenge("Reto2", "algo");

        // --- act ---
        SaveVectorMemoryCommand command = mapGeneratedChallenge();

        // --- assert ---
        thenChallengeMapped(command,
                "generated-challenge:unknown:Reto2",
                "Huly sugirio el reto: Reto2. Descripcion: algo.",
                "Reto2",
                "algo");
    }

    @Test
    @DisplayName("Arma el comando de actividad recomendada con todos los datos presentes")
    void toRecommendedActivityCommandShouldMapAllFieldsWhenPresent() {
        // --- arrange ---
        givenUserId(3L);
        givenConversation("conv-1");
        givenEmotionalEventId(99L);
        givenSuggestedAction(ActivityType.DIARY, 7L, "Meditar", "Respirar");

        // --- act ---
        SaveVectorMemoryCommand command = mapRecommendedActivity();

        // --- assert ---
        thenRecommendedMapped(command,
                "Huly recomendo la actividad: Meditar. Tipo: DIARY. Descripcion: Respirar.",
                "99",
                "99");
        thenRecommendedExtra(command, "7", "DIARY", "99");
    }

    @Test
    @DisplayName("Usa los valores por defecto y el id de usuario cuando la acción y el evento vienen vacíos")
    void toRecommendedActivityCommandShouldUseDefaultsAndUserIdWhenActionAndEventAreEmpty() {
        // --- arrange ---
        givenUserId(3L);
        givenConversation("conv-1");
        givenNoEmotionalEvent();
        givenActionWithNullFields();

        // --- act ---
        SaveVectorMemoryCommand command = mapRecommendedActivity();

        // --- assert ---
        thenRecommendedMapped(command,
                "Huly recomendo la actividad: Actividad. Tipo: UNKNOWN. Descripcion: .",
                "3",
                null);
        thenRecommendedExtra(command, "", "", "");
    }

    @Test
    @DisplayName("Deja el sourceId nulo en la actividad recomendada cuando no hay evento ni usuario")
    void toRecommendedActivityCommandShouldUseNullSourceIdWhenNoEventAndNoUser() {
        // --- arrange ---
        givenNoUser();
        givenConversation("conv-1");
        givenNoEmotionalEvent();
        givenSuggestedAction(ActivityType.DIARY, 7L, "Meditar", "Respirar");

        // --- act ---
        SaveVectorMemoryCommand command = mapRecommendedActivity();

        // --- assert ---
        thenRecommendedMapped(command,
                "Huly recomendo la actividad: Meditar. Tipo: DIARY. Descripcion: Respirar.",
                null,
                null);
    }

    @Test
    @DisplayName("Omite la razón cuando viene solo con espacios al armar el texto de la recomendación")
    void toCreateEmotionalEventRequestShouldOmitWhitespaceOnlyReason() {
        // --- arrange ---
        givenMessage("estresado");
        givenUserId(1L);
        givenAnalysis(EmotionType.STRESS, 0.9, 0.7, "calmarse");
        givenRecommendation(2L, "Respiración", "Práctica breve", "   ");

        // --- act ---
        CreateEmotionalEventRequest request = mapCreateEmotionalEvent();

        // --- assert ---
        thenGeneratedRecommendationIs(request, "Respiración: Práctica breve");
    }

    // --- arrange ---

    private void givenUserId(Long id) {
        this.userId = id;
    }

    private void givenNoUser() {
        this.userId = null;
    }

    private void givenConversation(String id) {
        this.conversationId = id;
    }

    private void givenNoConversation() {
        this.conversationId = null;
    }

    private void givenMessage(String text) {
        this.message = text;
    }

    private void givenChallenge(String title, String description) {
        this.generatedChallenge = new ChatReply.GeneratedChallenge(title, description);
    }

    private void givenEmotionalEventId(Long id) {
        this.emotionalEventId = id;
    }

    private void givenNoEmotionalEvent() {
        this.emotionalEventId = null;
    }

    private void givenSuggestedAction(ActivityType type, Long activityId, String title, String description) {
        this.suggestedAction = new SuggestedChatAction(type, activityId, title, description, "/api/activities", null);
    }

    private void givenActionWithNullFields() {
        this.suggestedAction = new SuggestedChatAction(null, null, null, null, null, null);
    }

    private void givenAnalysis(EmotionType emotion, double confidence, double intensity, String userGoal) {
        this.analysisResult = analysis(emotion, confidence, intensity, userGoal);
    }

    private void givenRecommendation(Long activityId, String title, String description, String reason) {
        this.recommendationItem = recommendation(activityId, title, description, reason);
    }

    // --- act ---

    private SaveVectorMemoryCommand mapUserMemory() {
        return mapper.toUserMemoryCommand(userId, conversationId, message);
    }

    private SaveVectorMemoryCommand mapGeneratedChallenge() {
        return mapper.toGeneratedChallengeCommand(userId, conversationId, generatedChallenge);
    }

    private SaveVectorMemoryCommand mapRecommendedActivity() {
        return mapper.toRecommendedActivityCommand(userId, conversationId, emotionalEventId, suggestedAction);
    }

    private CreateEmotionalEventRequest mapCreateEmotionalEvent() {
        return mapper.toCreateEmotionalEventRequest(message, userId, analysisResult, recommendationItem);
    }

    // --- assert ---

    private void thenUserMemoryMapped(SaveVectorMemoryCommand command, String expectedSourceId) {
        assertThat(command.sourceType()).isEqualTo(VectorMemorySource.CHATBOT);
        assertThat(command.sourceId()).isEqualTo(expectedSourceId);
        assertThat(command.source()).isEqualTo("USER_CHAT_MESSAGE");
        assertThat(command.contentType()).isEqualTo("CHAT_MESSAGE");
        assertThat(command.content()).isEqualTo(message);
        assertThat(command.conversationId()).isEqualTo(conversationId);
        assertThat(command.metadata())
                .containsEntry("createdFrom", "USER_MESSAGE")
                .containsEntry("feature", "CHATBOT");
    }

    private void thenChallengeMapped(
            SaveVectorMemoryCommand command,
            String expectedSourceId,
            String expectedContent,
            String expectedTitle,
            String expectedDescription) {
        assertThat(command.sourceType()).isEqualTo(VectorMemorySource.CHATBOT);
        assertThat(command.sourceId()).isEqualTo(expectedSourceId);
        assertThat(command.source()).isEqualTo("GENERATED_CHALLENGE");
        assertThat(command.content()).isEqualTo(expectedContent);
        assertThat(command.conversationId()).isEqualTo(conversationId);
        assertThat(command.metadata())
                .containsEntry("createdFrom", "USER_MESSAGE")
                .containsEntry("feature", "CHATBOT_CHALLENGE")
                .containsEntry("challengeTitle", expectedTitle)
                .containsEntry("challengeDescription", expectedDescription);
    }

    private void thenRecommendedMapped(
            SaveVectorMemoryCommand command,
            String expectedContent,
            String expectedSourceId,
            String expectedMessageId) {
        assertThat(command.sourceType()).isEqualTo(VectorMemorySource.CHATBOT);
        assertThat(command.source()).isEqualTo("RECOMMENDED_ACTIVITY");
        assertThat(command.content()).isEqualTo(expectedContent);
        assertThat(command.sourceId()).isEqualTo(expectedSourceId);
        assertThat(command.messageId()).isEqualTo(expectedMessageId);
        assertThat(command.conversationId()).isEqualTo(conversationId);
    }

    private void thenRecommendedExtra(
            SaveVectorMemoryCommand command,
            String expectedActivityId,
            String expectedActivityType,
            String expectedEmotionalEventId) {
        assertThat(command.metadata())
                .containsEntry("createdFrom", "USER_MESSAGE")
                .containsEntry("feature", "CHATBOT_ACTIVITY_RECOMMENDATION")
                .containsEntry("activityId", expectedActivityId)
                .containsEntry("activityType", expectedActivityType)
                .containsEntry("emotionalEventId", expectedEmotionalEventId);
    }

    private void thenGeneratedRecommendationIs(CreateEmotionalEventRequest request, String expected) {
        assertThat(request.generatedRecommendation()).isEqualTo(expected);
    }

    private EmotionalAnalysisResult analysis(
            EmotionType emotion,
            double confidence,
            double intensity,
            String userGoal) {
        return new EmotionalAnalysisResult(
                true, emotion, confidence, -0.5, 0.5, -0.5, intensity, userGoal, "motivo");
    }

    private EmotionalRecommendationItem recommendation(
            Long activityId,
            String title,
            String description,
            String reason) {
        return new EmotionalRecommendationItem(
                activityId, ActivityType.DIARY, title, description, 0.9, reason, "/diary");
    }

    private EmotionalEventResponse eventResponse(Long id) {
        Instant now = Instant.now();
        return new EmotionalEventResponse(
                id,
                1L,
                EmotionalEventSource.CHATBOT,
                null,
                "GRIEF",
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                7L,
                null,
                (RecommendationDecision) null,
                null,
                null,
                now,
                now
        );
    }
}
