package com.huly.backend.infrastructure.presentation.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.huly.backend.domain.dto.chat.ChatHistoryRequest;
import com.huly.backend.domain.dto.chat.ChatHistoryResponse;
import com.huly.backend.domain.dto.chat.ChatMessageRequest;
import com.huly.backend.domain.dto.chat.ChatReplyResponse;
import com.huly.backend.domain.dto.chat.GeneratedChallengeResponse;
import com.huly.backend.domain.dto.chat.SuggestedActionResponse;
import com.huly.backend.domain.exception.BusinessRuleException;
import com.huly.backend.domain.model.enums.ActivityType;
import com.huly.backend.domain.model.enums.EmotionType;
import com.huly.backend.domain.model.enums.MessageRole;
import com.huly.backend.domain.service.chat.ChatQuotaService;
import com.huly.backend.domain.useCase.chat.AudioChatUseCase;
import com.huly.backend.domain.useCase.chat.ChatUseCase;
import com.huly.backend.domain.useCase.chat.ListChatHistoryUseCase;
import com.huly.backend.domain.useCase.chat.SaveChallengeDecisionUseCase;
import com.huly.backend.infrastructure.presentation.dto.chat.ChatChallengeDecisionRequest;
import com.huly.backend.infrastructure.presentation.dto.chat.ChatRequest;
import com.huly.backend.infrastructure.presentation.exception.GlobalExceptionHandler;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.method.annotation.AuthenticationPrincipalArgumentResolver;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.Instant;
import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class ChatControllerTest {

    private static final Long USER_ID = 1L;
    private static final Instant CREATED_AT = Instant.parse("2024-01-01T00:00:00Z");

    private final ObjectMapper objectMapper = new ObjectMapper();

    private MockMvc mockMvc;
    private ChatUseCase chatUseCase;
    private AudioChatUseCase audioChatUseCase;
    private ListChatHistoryUseCase listChatHistoryUseCase;
    private SaveChallengeDecisionUseCase saveChallengeDecisionUseCase;
    private ChatQuotaService chatQuotaService;

    private String requestJson;
    private MockMultipartFile audioFile;

    @BeforeEach
    void setUp() {
        chatUseCase = mock(ChatUseCase.class);
        audioChatUseCase = mock(AudioChatUseCase.class);
        listChatHistoryUseCase = mock(ListChatHistoryUseCase.class);
        saveChallengeDecisionUseCase = mock(SaveChallengeDecisionUseCase.class);
        chatQuotaService = mock(ChatQuotaService.class);
        when(chatQuotaService.getRemainingQuota(any())).thenReturn(new ChatQuotaService.RemainingQuota(null, null));
        when(chatQuotaService.getRemainingAudioQuota(any())).thenReturn(new ChatQuotaService.RemainingAudioQuota(null, null));

        ChatController controller = new ChatController(
                chatUseCase,
                audioChatUseCase,
                listChatHistoryUseCase,
                saveChallengeDecisionUseCase,
                chatQuotaService
        );
        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setCustomArgumentResolvers(new AuthenticationPrincipalArgumentResolver())
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
        authenticateAs(String.valueOf(USER_ID));
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    @DisplayName("Devuelve 200 con el contenido de la respuesta cuando el request es válido")
    void chatShouldReturn200WithReplyContentWhenRequestIsValid() throws Exception {
        givenChatReply(reply("todo bien", EmotionType.JOY, 9, false, null));
        givenChatRequest("hola", "conv-1");

        ResultActions result = performChat();

        thenOkWithJoyReply(result);
    }

    @Test
    @DisplayName("Devuelve 200 sin emoción ni metadata cuando la respuesta no trae emoción")
    void chatShouldReturn200WithNullEmotionWhenReplyHasNoEmotion() throws Exception {
        givenChatReply(reply("respuesta", null, null, null, null));
        givenChatRequest("msg", "conv-1");

        ResultActions result = performChat();

        thenOkWithoutEmotionOrMetadata(result);
    }

    @Test
    @DisplayName("Devuelve 200 con metadata de riesgo cuando se detecta riesgo")
    void chatShouldReturn200WithMetadataWhenRiskDetected() throws Exception {
        givenChatReply(reply("cuidado", EmotionType.FEAR, 8, true, "suicidio"));
        givenChatRequest("estoy mal", "conv-2");

        ResultActions result = performChat();

        thenOkWithRiskMetadata(result);
    }

    @Test
    @DisplayName("Devuelve la acción sugerida cuando existe una recomendación")
    void chatShouldReturnSuggestedActionWhenRecommendationExists() throws Exception {
        givenChatReply(replyWith("te acompaño", EmotionType.SADNESS, 9, false, null,
                suggestedAction(ActivityType.DIARY, 2L, "Diario emocional", "Un espacio para ordenar pensamientos", "/api/activities", 15L),
                null));
        givenChatRequest("estoy triste", "conv-2");

        ResultActions result = performChat();

        thenOkWithSuggestedAction(result);
    }

    @Test
    @DisplayName("Devuelve 200 sin metadata cuando riskDetected es null")
    void chatShouldReturn200WithNullMetadataWhenRiskDetectedIsNull() throws Exception {
        givenChatReply(reply("respuesta", EmotionType.CALM, 3, null, null));
        givenChatRequest("msg", "conv-1");

        ResultActions result = performChat();

        thenOkWithoutMetadata(result);
    }

    @Test
    @DisplayName("Devuelve el reto generado cuando la respuesta lo incluye")
    void chatShouldReturnGeneratedChallengeWhenReplyIncludesIt() throws Exception {
        givenChatReply(replyWith("con reto", EmotionType.CALM, 5, false, null, null,
                challenge("Reto diario", "Salí a caminar")));
        givenChatRequest("dame un reto", "conv-1");

        ResultActions result = performChat();

        thenOkWithGeneratedChallenge(result);
    }

    @Test
    @DisplayName("Omite el tipo y el id de la acción cuando vienen en null")
    void chatShouldOmitActionTypeAndIdWhenSuggestedActionHasNullTypeAndId() throws Exception {
        givenChatReply(replyWith("acción sin tipo", EmotionType.CALM, 4, false, null,
                suggestedAction(null, null, "Sin tipo", "Descripción", "/api/actions", 20L), null));
        givenChatRequest("recomendame algo", "conv-1");

        ResultActions result = performChat();

        thenOkWithoutActionTypeAndId(result);
    }

    @Test
    @DisplayName("Devuelve los mensajes restantes cuando el plan tiene límite")
    void chatShouldReturnRemainingQuotaWhenPlanIsLimited() throws Exception {
        givenChatReply(reply("ok", null, null, null, null));
        givenRemainingQuota(2, null);
        givenChatRequest("hola", "conv-1");

        ResultActions result = performChat();

        thenOkWithRemainingMessages(result, 2);
    }

    @Test
    @DisplayName("Devuelve el mensaje de límite cuando se agotó la cuota")
    void chatShouldReturnLimitMessageWhenQuotaIsExhausted() throws Exception {
        givenChatReply(reply("ok", null, null, null, null));
        givenRemainingQuota(0, "Alcanzaste el límite diario");
        givenChatRequest("hola", "conv-1");

        ResultActions result = performChat();

        thenOkWithExhaustedQuota(result, "Alcanzaste el límite diario");
    }

    @Test
    @DisplayName("Devuelve 400 cuando el mensaje está vacío")
    void chatShouldReturn400WhenMessageIsBlank() throws Exception {
        givenChatRequest("", "conv-1");

        ResultActions result = performChat();

        thenBadRequest(result);
    }

    @Test
    @DisplayName("Devuelve 400 cuando el conversationId está vacío")
    void chatShouldReturn400WhenConversationIdIsBlank() throws Exception {
        givenChatRequest("mensaje", "");

        ResultActions result = performChat();

        thenBadRequest(result);
    }

    @Test
    @DisplayName("Devuelve 401 cuando no está autenticado")
    void chatShouldReturn401WhenNotAuthenticated() throws Exception {
        givenChatRequest("hola", "conv-1");
        givenNoAuthentication();

        ResultActions result = performChat();

        thenUnauthorized(result);
    }

    @Test
    @DisplayName("Devuelve 500 cuando el usuario del principal no es numérico")
    void chatShouldReturn500WhenPrincipalUsernameIsNotNumeric() throws Exception {
        givenChatRequest("hola", "conv-1");
        authenticateAs("not-a-number");

        ResultActions result = performChat();

        thenInternalServerError(result);
    }

    @Test
    @DisplayName("Usa el id del usuario autenticado al enviar un mensaje")
    void chatShouldUseAuthenticatedUserId() throws Exception {
        givenChatReply(reply("ok", null, null, null, null));
        givenChatRequest("hola", "conv-1");

        ResultActions result = performChat();

        thenChatExecutedWith(result, "hola", "conv-1", USER_ID);
    }

    @Test
    @DisplayName("Devuelve 200 con los mensajes paginados")
    void getHistoryShouldReturn200WithPagedMessages() throws Exception {
        givenHistory(historyPage(List.of(historyMessage(1L, MessageRole.USER, "hola", false, EmotionType.JOY)), 0, 1, 1, 1));

        ResultActions result = performGetHistory("conv-1");

        thenOkWithPagedUserMessage(result);
    }

    @Test
    @DisplayName("Devuelve tarjetas y decisiones cuando el mensaje del asistente las incluye")
    void getHistoryShouldReturnCardsAndDecisionsWhenAssistantMessageIncludesThem() throws Exception {
        givenHistory(historyPage(List.of(historyMessage(3L, MessageRole.ASSISTANT, "Te sugiero esto", false, EmotionType.CALM,
                suggestedAction(ActivityType.BREATHING, 4L, "Respiracion guiada", "Respira lento", "/guided-breathing", 12L),
                challenge("Reto breve", "Tomate cinco minutos"), "ACCEPTED", "REJECTED")), 0, 1, 1, 1));

        ResultActions result = performGetHistory("conv-1");

        thenOkWithCardsAndDecisions(result);
    }

    @Test
    @DisplayName("Omite las decisiones cuando vienen en blanco")
    void getHistoryShouldOmitDecisionsWhenTheyAreBlank() throws Exception {
        givenHistory(historyPage(List.of(historyMessage(5L, MessageRole.ASSISTANT, "Te sugiero esto", false, EmotionType.CALM,
                null, null, "   ", "   ")), 0, 1, 1, 1));

        ResultActions result = performGetHistory("conv-1");

        thenOkWithoutDecisions(result);
    }

    @Test
    @DisplayName("Devuelve 200 con página vacía cuando no hay mensajes")
    void getHistoryShouldReturn200WithEmptyPageWhenNoMessages() throws Exception {
        givenHistory(historyPage(List.of(), 0, 20, 0, 0));

        ResultActions result = performGetHistory("conv-vacia");

        thenOkWithEmptyContent(result);
    }

    @Test
    @DisplayName("Devuelve 200 sin rol ni emoción cuando el mensaje tiene nulls")
    void getHistoryShouldReturn200WithNullRoleAndEmotionWhenMessageHasNulls() throws Exception {
        givenHistory(historyPage(List.of(historyMessage(2L, null, "mensaje", null, null)), 0, 1, 1, 1));

        ResultActions result = performGetHistory("conv-1");

        thenOkWithoutRoleAndEmotion(result);
    }

    @Test
    @DisplayName("Usa la paginación por defecto cuando no se envían parámetros")
    void getHistoryShouldUseDefaultPaginationWhenNoParamsProvided() throws Exception {
        givenHistory(historyPage(List.of(), 0, 20, 0, 0));

        ResultActions result = performGetHistory("conv-1");

        thenHistoryQueriedWith(result, 0, 20, "conv-1", USER_ID);
    }

    @Test
    @DisplayName("Reenvía el page y size personalizados cuando se envían como parámetros")
    void getHistoryShouldForwardCustomPageAndSizeWhenParamsProvided() throws Exception {
        givenHistory(historyPage(List.of(), 2, 5, 0, 0));

        ResultActions result = performGetHistory("conv-1", 2, 5);

        thenHistoryQueriedWith(result, 2, 5, "conv-1", USER_ID);
    }

    @Test
    @DisplayName("Devuelve la metadata de paginación")
    void getHistoryShouldReturnPaginationMetadata() throws Exception {
        givenHistory(historyPage(List.of(historyMessage(1L, MessageRole.ASSISTANT, "resp", false, EmotionType.CALM)), 0, 1, 1, 1));

        ResultActions result = performGetHistory("conv-1");

        thenOkWithPaginationMetadata(result);
    }

    @Test
    @DisplayName("Devuelve 200 cuando el audio y el conversationId son válidos")
    void sendAudioMessageShouldReturn200WhenAudioAndConversationIdAreValid() throws Exception {
        givenAudioReply(reply("entendí tu mensaje de voz", null, null, false, null), "conv-1", USER_ID);
        givenAudioFile("fake".getBytes());

        ResultActions result = performSendAudio("conv-1");

        thenOkWithAudioReply(result);
    }

    @Test
    @DisplayName("Delega en AudioChatUseCase con el id del usuario autenticado")
    void sendAudioMessageShouldDelegateToAudioChatUseCaseWithAuthenticatedUserId() throws Exception {
        givenAudioReply(reply("ok", null, null, null, null), "conv-1", USER_ID);
        givenAudioFile("fake".getBytes());

        ResultActions result = performSendAudio("conv-1");

        thenAudioDelegated(result, "conv-1", USER_ID);
    }

    @Test
    @DisplayName("Devuelve los audios restantes cuando el plan tiene límite")
    void sendAudioMessageShouldReturnRemainingAudioQuotaWhenPlanIsLimited() throws Exception {
        givenAudioReply(reply("audio ok", null, null, null, null), "conv-1", USER_ID);
        givenRemainingAudioQuota(1, null);
        givenAudioFile("fake".getBytes());

        ResultActions result = performSendAudio("conv-1");

        thenOkWithRemainingAudioMessages(result, 1);
    }

    @Test
    @DisplayName("Devuelve 200 cuando el audio no tiene contenido")
    void sendAudioMessageShouldReturn200WhenAudioIsEmpty() throws Exception {
        givenAudioReply(reply("audio vacío", null, null, null, null), "conv-1", USER_ID);
        givenAudioFile(new byte[0]);

        ResultActions result = performSendAudio("conv-1");

        thenOk(result);
    }

    @Test
    @DisplayName("Devuelve 400 cuando se superó el límite de audios")
    void sendAudioMessageShouldReturn400WhenAudioLimitExceeded() throws Exception {
        givenAudioLimitExceeded();
        givenAudioFile("fake".getBytes());

        ResultActions result = performSendAudio("conv-1");

        thenBadRequest(result);
    }

    // MissingServletRequestParameterException no está mapeada en GlobalExceptionHandler
    // (solo MethodArgumentNotValidException lo está), cae al catch-all → 500
    @Test
    @DisplayName("Devuelve 500 cuando falta el conversationId del audio")
    void sendAudioMessageShouldReturn500WhenConversationIdIsMissing() throws Exception {
        givenAudioFile("fake".getBytes());

        ResultActions result = performSendAudioWithoutConversationId();

        thenInternalServerError(result);
    }

    @Test
    @DisplayName("Devuelve 204 y delega en SaveChallengeDecisionUseCase")
    void challengeDecisionShouldReturn204AndDelegateToSaveChallengeDecisionUseCase() throws Exception {
        givenChallengeDecisionRequest("conv-1", "title", "desc", "ACCEPTED");

        ResultActions result = performChallengeDecision();

        thenChallengeDecisionSaved(result, USER_ID, "title", "ACCEPTED", "desc", "conv-1");
    }

    @Test
    @DisplayName("Devuelve 400 cuando el título de la decisión está en blanco")
    void challengeDecisionShouldReturn400WhenTitleIsBlank() throws Exception {
        givenChallengeDecisionRequest("conv-1", "", "desc", "ACCEPTED");

        ResultActions result = performChallengeDecision();

        thenBadRequest(result);
    }

    // --- arrange ---
    private void authenticateAs(String username) {
        UserDetails userDetails = new User(username, "", Collections.emptyList());
        SecurityContextHolder.getContext().setAuthentication(new TestingAuthenticationToken(userDetails, null));
    }

    private void givenNoAuthentication() {
        SecurityContextHolder.clearContext();
    }

    private void givenChatReply(ChatReplyResponse reply) {
        when(chatUseCase.execute(any(ChatMessageRequest.class))).thenReturn(reply);
    }

    private void givenAudioReply(ChatReplyResponse reply, String conversationId, Long userId) {
        when(audioChatUseCase.execute(any(), eq(conversationId), eq(userId))).thenReturn(reply);
    }

    private void givenAudioLimitExceeded() {
        doThrow(new BusinessRuleException("Alcanzaste el límite de audios diarios"))
                .when(chatQuotaService).assertWithinAudioLimit(any());
    }

    private void givenHistory(ChatHistoryResponse response) {
        when(listChatHistoryUseCase.execute(any(ChatHistoryRequest.class))).thenReturn(response);
    }

    private void givenRemainingQuota(Integer remaining, String limitMessage) {
        when(chatQuotaService.getRemainingQuota(any()))
                .thenReturn(new ChatQuotaService.RemainingQuota(remaining, limitMessage));
    }

    private void givenRemainingAudioQuota(Integer remaining, String limitMessage) {
        when(chatQuotaService.getRemainingAudioQuota(any()))
                .thenReturn(new ChatQuotaService.RemainingAudioQuota(remaining, limitMessage));
    }

    private void givenChatRequest(String message, String conversationId) throws Exception {
        requestJson = objectMapper.writeValueAsString(new ChatRequest(message, conversationId));
    }

    private void givenChallengeDecisionRequest(String conversationId, String title, String description, String decision) throws Exception {
        requestJson = objectMapper.writeValueAsString(
                new ChatChallengeDecisionRequest(conversationId, title, description, decision));
    }

    private void givenAudioFile(byte[] content) {
        audioFile = new MockMultipartFile("audio", "recording.webm", "audio/webm", content);
    }

    private ChatReplyResponse reply(String content, EmotionType emotion, Integer intensity,
                                    Boolean riskDetected, String matchedWord) {
        return new ChatReplyResponse(content, emotion, intensity, riskDetected, matchedWord, null, null);
    }

    private ChatReplyResponse replyWith(String content, EmotionType emotion, Integer intensity, Boolean riskDetected,
                                        String matchedWord, SuggestedActionResponse action, GeneratedChallengeResponse challenge) {
        return new ChatReplyResponse(content, emotion, intensity, riskDetected, matchedWord, action, challenge);
    }

    private SuggestedActionResponse suggestedAction(ActivityType type, Long activityId, String title,
                                                    String description, String actionUrl, Long emotionalEventId) {
        return new SuggestedActionResponse(type, activityId, title, description, actionUrl, emotionalEventId);
    }

    private GeneratedChallengeResponse challenge(String title, String description) {
        return new GeneratedChallengeResponse(title, description);
    }

    private ChatHistoryResponse.Message historyMessage(Long id, MessageRole role, String content,
                                                       Boolean riskDetected, EmotionType emotion) {
        return historyMessage(id, role, content, riskDetected, emotion, null, null, null, null);
    }

    private ChatHistoryResponse.Message historyMessage(Long id, MessageRole role, String content, Boolean riskDetected,
                                                       EmotionType emotion, SuggestedActionResponse action,
                                                       GeneratedChallengeResponse challenge, String actionDecision,
                                                       String challengeDecision) {
        return new ChatHistoryResponse.Message(
                id, role, content, riskDetected, emotion, CREATED_AT, action, challenge, actionDecision, challengeDecision);
    }

    private ChatHistoryResponse historyPage(List<ChatHistoryResponse.Message> content, int pageNumber,
                                            int pageSize, long totalElements, int totalPages) {
        return new ChatHistoryResponse(content, pageNumber, pageSize, totalElements, totalPages, true, true);
    }

    // --- act ---
    private ResultActions performChat() throws Exception {
        return mockMvc.perform(post("/api/chat")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestJson));
    }

    private ResultActions performChallengeDecision() throws Exception {
        return mockMvc.perform(post("/api/chat/challenge-decision")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestJson));
    }

    private ResultActions performGetHistory(String conversationId) throws Exception {
        return mockMvc.perform(get("/api/chat/{conversationId}/messages", conversationId));
    }

    private ResultActions performGetHistory(String conversationId, int page, int size) throws Exception {
        return mockMvc.perform(get("/api/chat/{conversationId}/messages", conversationId)
                .param("page", String.valueOf(page))
                .param("size", String.valueOf(size)));
    }

    private ResultActions performSendAudio(String conversationId) throws Exception {
        return mockMvc.perform(multipart("/api/chat/audio")
                .file(audioFile)
                .param("conversationId", conversationId));
    }

    private ResultActions performSendAudioWithoutConversationId() throws Exception {
        return mockMvc.perform(multipart("/api/chat/audio")
                .file(audioFile));
    }

    // --- assert ---
    private void thenOk(ResultActions result) throws Exception {
        result.andExpect(status().isOk());
    }

    private void thenBadRequest(ResultActions result) throws Exception {
        result.andExpect(status().isBadRequest());
    }

    private void thenUnauthorized(ResultActions result) throws Exception {
        result.andExpect(status().isUnauthorized());
    }

    private void thenInternalServerError(ResultActions result) throws Exception {
        result.andExpect(status().isInternalServerError());
    }

    private void thenOkWithJoyReply(ResultActions result) throws Exception {
        result.andExpect(status().isOk())
                .andExpect(jsonPath("$.huly_reply").value("todo bien"))
                .andExpect(jsonPath("$.detected_emotion").value("JOY"))
                .andExpect(jsonPath("$.intensity").value(9));
    }

    private void thenOkWithoutEmotionOrMetadata(ResultActions result) throws Exception {
        result.andExpect(status().isOk())
                .andExpect(jsonPath("$.detected_emotion").doesNotExist())
                .andExpect(jsonPath("$.metadata").doesNotExist());
    }

    private void thenOkWithRiskMetadata(ResultActions result) throws Exception {
        result.andExpect(status().isOk())
                .andExpect(jsonPath("$.metadata.risk_detected").value(true))
                .andExpect(jsonPath("$.metadata.matched_word").value("suicidio"));
    }

    private void thenOkWithSuggestedAction(ResultActions result) throws Exception {
        result.andExpect(status().isOk())
                .andExpect(jsonPath("$.suggested_action.type").value("DIARY"))
                .andExpect(jsonPath("$.suggested_action.action_id").value("2"))
                .andExpect(jsonPath("$.suggested_action.title").value("Diario emocional"))
                .andExpect(jsonPath("$.suggested_action.emotional_event_id").value(15));
    }

    private void thenOkWithoutMetadata(ResultActions result) throws Exception {
        result.andExpect(status().isOk())
                .andExpect(jsonPath("$.metadata").doesNotExist());
    }

    private void thenOkWithGeneratedChallenge(ResultActions result) throws Exception {
        result.andExpect(status().isOk())
                .andExpect(jsonPath("$.generated_challenge.title").value("Reto diario"))
                .andExpect(jsonPath("$.generated_challenge.description").value("Salí a caminar"));
    }

    private void thenOkWithoutActionTypeAndId(ResultActions result) throws Exception {
        result.andExpect(status().isOk())
                .andExpect(jsonPath("$.suggested_action.type").doesNotExist())
                .andExpect(jsonPath("$.suggested_action.action_id").doesNotExist())
                .andExpect(jsonPath("$.suggested_action.title").value("Sin tipo"));
    }

    private void thenOkWithRemainingMessages(ResultActions result, int remaining) throws Exception {
        result.andExpect(status().isOk())
                .andExpect(jsonPath("$.remaining_messages").value(remaining))
                .andExpect(jsonPath("$.limit_message").doesNotExist());
    }

    private void thenOkWithExhaustedQuota(ResultActions result, String limitMessage) throws Exception {
        result.andExpect(status().isOk())
                .andExpect(jsonPath("$.remaining_messages").value(0))
                .andExpect(jsonPath("$.limit_message").value(limitMessage));
    }

    private void thenChatExecutedWith(ResultActions result, String message, String conversationId, Long userId) throws Exception {
        result.andExpect(status().isOk());
        verify(chatUseCase).execute(argThat(req ->
                message.equals(req.message())
                        && conversationId.equals(req.conversationId())
                        && userId.equals(req.userId())));
    }

    private void thenOkWithPagedUserMessage(ResultActions result) throws Exception {
        result.andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").value(1))
                .andExpect(jsonPath("$.content[0].role").value("USER"))
                .andExpect(jsonPath("$.content[0].content").value("hola"))
                .andExpect(jsonPath("$.content[0].risk_detected").value(false))
                .andExpect(jsonPath("$.content[0].detected_emotion").value("JOY"))
                .andExpect(jsonPath("$.total_elements").value(1))
                .andExpect(jsonPath("$.first").value(true))
                .andExpect(jsonPath("$.last").value(true));
    }

    private void thenOkWithCardsAndDecisions(ResultActions result) throws Exception {
        result.andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].suggested_action.type").value("BREATHING"))
                .andExpect(jsonPath("$.content[0].suggested_action.emotional_event_id").value(12))
                .andExpect(jsonPath("$.content[0].generated_challenge.title").value("Reto breve"))
                .andExpect(jsonPath("$.content[0].suggested_action_decision").value("accepted"))
                .andExpect(jsonPath("$.content[0].challenge_decision").value("rejected"));
    }

    private void thenOkWithoutDecisions(ResultActions result) throws Exception {
        result.andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].suggested_action_decision").doesNotExist())
                .andExpect(jsonPath("$.content[0].challenge_decision").doesNotExist());
    }

    private void thenOkWithEmptyContent(ResultActions result) throws Exception {
        result.andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isEmpty())
                .andExpect(jsonPath("$.total_elements").value(0));
    }

    private void thenOkWithoutRoleAndEmotion(ResultActions result) throws Exception {
        result.andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].role").doesNotExist())
                .andExpect(jsonPath("$.content[0].detected_emotion").doesNotExist());
    }

    private void thenHistoryQueriedWith(ResultActions result, int page, int size, String conversationId, Long userId) throws Exception {
        result.andExpect(status().isOk());
        verify(listChatHistoryUseCase).execute(argThat(req ->
                req.page() == page
                        && req.size() == size
                        && conversationId.equals(req.conversationId())
                        && userId.equals(req.userId())));
    }

    private void thenOkWithPaginationMetadata(ResultActions result) throws Exception {
        result.andExpect(status().isOk())
                .andExpect(jsonPath("$.page_number").value(0))
                .andExpect(jsonPath("$.page_size").value(1))
                .andExpect(jsonPath("$.total_elements").value(1))
                .andExpect(jsonPath("$.total_pages").value(1));
    }

    private void thenOkWithAudioReply(ResultActions result) throws Exception {
        result.andExpect(status().isOk())
                .andExpect(jsonPath("$.huly_reply").value("entendí tu mensaje de voz"));
    }

    private void thenAudioDelegated(ResultActions result, String conversationId, Long userId) throws Exception {
        result.andExpect(status().isOk());
        verify(audioChatUseCase).execute(any(), eq(conversationId), eq(userId));
    }

    private void thenOkWithRemainingAudioMessages(ResultActions result, int remaining) throws Exception {
        result.andExpect(status().isOk())
                .andExpect(jsonPath("$.remaining_audio_messages").value(remaining));
    }

    private void thenChallengeDecisionSaved(ResultActions result, Long userId, String title, String decision,
                                            String description, String conversationId) throws Exception {
        result.andExpect(status().isNoContent());
        verify(saveChallengeDecisionUseCase).execute(eq(userId), eq(title), eq(decision), eq(description), eq(conversationId));
    }
}
