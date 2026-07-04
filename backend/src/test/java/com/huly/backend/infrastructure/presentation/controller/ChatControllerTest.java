package com.huly.backend.infrastructure.presentation.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.huly.backend.domain.dto.chat.ChatHistoryRequest;
import com.huly.backend.domain.dto.chat.ChatHistoryResponse;
import com.huly.backend.domain.dto.chat.ChatMessageRequest;
import com.huly.backend.domain.dto.chat.ChatReplyResponse;
import com.huly.backend.domain.dto.chat.GeneratedChallengeResponse;
import com.huly.backend.domain.dto.chat.SuggestedActionResponse;
import com.huly.backend.domain.model.enums.ActivityType;
import com.huly.backend.domain.model.enums.EmotionType;
import com.huly.backend.domain.model.enums.MessageRole;
import com.huly.backend.domain.service.chat.ChatQuotaService;
import com.huly.backend.domain.useCase.chat.AudioChatUseCase;
import com.huly.backend.domain.useCase.chat.ChatUseCase;
import com.huly.backend.domain.useCase.chat.ListChatHistoryUseCase;
import com.huly.backend.domain.useCase.chat.SaveChallengeDecisionUseCase;
import com.huly.backend.infrastructure.presentation.dto.chat.ChatRequest;
import com.huly.backend.infrastructure.presentation.exception.GlobalExceptionHandler;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.method.annotation.AuthenticationPrincipalArgumentResolver;
import org.springframework.test.web.servlet.MockMvc;
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

    private MockMvc mockMvc;
    private final ObjectMapper objectMapper = new ObjectMapper();

    private ChatUseCase chatUseCase;
    private AudioChatUseCase audioChatUseCase;
    private ListChatHistoryUseCase listChatHistoryUseCase;
    private SaveChallengeDecisionUseCase saveChallengeDecisionUseCase;
    private ChatQuotaService chatQuotaService;

    private static final Long USER_ID = 1L;

    @BeforeEach
    void setUp() {
        chatUseCase = mock(ChatUseCase.class);
        audioChatUseCase = mock(AudioChatUseCase.class);
        listChatHistoryUseCase = mock(ListChatHistoryUseCase.class);
        saveChallengeDecisionUseCase = mock(SaveChallengeDecisionUseCase.class);
        chatQuotaService = mock(ChatQuotaService.class);
        when(chatQuotaService.getRemainingQuota(any())).thenReturn(new ChatQuotaService.RemainingQuota(null, null));
        when(chatQuotaService.getRemainingAudioQuota(any())).thenReturn(new ChatQuotaService.RemainingAudioQuota(null, null));

        UserDetails userDetails = new User(String.valueOf(USER_ID), "", Collections.emptyList());
        SecurityContextHolder.getContext().setAuthentication(
                new TestingAuthenticationToken(userDetails, null));

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
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    private ChatReplyResponse reply(String content, EmotionType emotion, Integer intensity,
                                    Boolean riskDetected, String matchedWord) {
        return new ChatReplyResponse(content, emotion, intensity, riskDetected, matchedWord, null, null);
    }

    @Test
    void chat_shouldReturn200WithReplyContent_whenRequestIsValid() throws Exception {
        ChatReplyResponse reply = reply("todo bien", EmotionType.JOY, 9, false, null);
        when(chatUseCase.execute(any(ChatMessageRequest.class))).thenReturn(reply);

        mockMvc.perform(post("/api/chat")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new ChatRequest("hola", "conv-1"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.huly_reply").value("todo bien"))
                .andExpect(jsonPath("$.detected_emotion").value("JOY"))
                .andExpect(jsonPath("$.intensity").value(9));
    }

    @Test
    void chat_shouldReturn200WithNullEmotion_whenReplyHasNoEmotion() throws Exception {
        ChatReplyResponse reply = reply("respuesta", null, null, null, null);
        when(chatUseCase.execute(any(ChatMessageRequest.class))).thenReturn(reply);

        mockMvc.perform(post("/api/chat")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new ChatRequest("msg", "conv-1"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.detected_emotion").doesNotExist())
                .andExpect(jsonPath("$.metadata").doesNotExist());
    }

    @Test
    void chat_shouldReturn200WithMetadata_whenRiskDetected() throws Exception {
        ChatReplyResponse reply = reply("cuidado", EmotionType.FEAR, 8, true, "suicidio");
        when(chatUseCase.execute(any(ChatMessageRequest.class))).thenReturn(reply);

        mockMvc.perform(post("/api/chat")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new ChatRequest("estoy mal", "conv-2"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.metadata.risk_detected").value(true))
                .andExpect(jsonPath("$.metadata.matched_word").value("suicidio"));
    }

    @Test
    void chat_shouldReturnSuggestedAction_whenRecommendationExists() throws Exception {
        SuggestedActionResponse action = new SuggestedActionResponse(
                ActivityType.DIARY,
                2L,
                "Diario emocional",
                "Un espacio para ordenar pensamientos",
                "/api/activities",
                15L
        );
        ChatReplyResponse reply = new ChatReplyResponse("te acompaño", EmotionType.SADNESS, 9, false, null, action, null);
        when(chatUseCase.execute(any(ChatMessageRequest.class))).thenReturn(reply);

        mockMvc.perform(post("/api/chat")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new ChatRequest("estoy triste", "conv-2"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.suggested_action.type").value("DIARY"))
                .andExpect(jsonPath("$.suggested_action.action_id").value("2"))
                .andExpect(jsonPath("$.suggested_action.title").value("Diario emocional"))
                .andExpect(jsonPath("$.suggested_action.emotional_event_id").value(15));
    }

    @Test
    void chat_shouldReturn200WithNullMetadata_whenRiskDetectedIsNull() throws Exception {
        ChatReplyResponse reply = reply("respuesta", EmotionType.CALM, 3, null, null);
        when(chatUseCase.execute(any(ChatMessageRequest.class))).thenReturn(reply);

        mockMvc.perform(post("/api/chat")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new ChatRequest("msg", "conv-1"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.metadata").doesNotExist());
    }

    @Test
    void chat_shouldReturn400_whenMessageIsBlank() throws Exception {
        mockMvc.perform(post("/api/chat")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new ChatRequest("", "conv-1"))))
                .andExpect(status().isBadRequest());
    }

    @Test
    void chat_shouldReturn400_whenConversationIdIsBlank() throws Exception {
        mockMvc.perform(post("/api/chat")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new ChatRequest("mensaje", ""))))
                .andExpect(status().isBadRequest());
    }

    @Test
    void chat_shouldUseAuthenticatedUserId() throws Exception {
        when(chatUseCase.execute(any(ChatMessageRequest.class))).thenReturn(reply("ok", null, null, null, null));

        mockMvc.perform(post("/api/chat")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new ChatRequest("hola", "conv-1"))))
                .andExpect(status().isOk());

        verify(chatUseCase).execute(argThat(req ->
                "hola".equals(req.message())
                        && "conv-1".equals(req.conversationId())
                        && USER_ID.equals(req.userId())));
    }

    @Test
    void getHistory_shouldReturn200WithPagedMessages() throws Exception {
        ChatHistoryResponse.Message msg = new ChatHistoryResponse.Message(
                1L, MessageRole.USER, "hola", false, EmotionType.JOY,
                Instant.parse("2024-01-01T00:00:00Z"), null, null, null, null);
        ChatHistoryResponse page = new ChatHistoryResponse(List.of(msg), 0, 1, 1, 1, true, true);
        when(listChatHistoryUseCase.execute(any(ChatHistoryRequest.class))).thenReturn(page);

        mockMvc.perform(get("/api/chat/conv-1/messages"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").value(1))
                .andExpect(jsonPath("$.content[0].role").value("USER"))
                .andExpect(jsonPath("$.content[0].content").value("hola"))
                .andExpect(jsonPath("$.content[0].risk_detected").value(false))
                .andExpect(jsonPath("$.content[0].detected_emotion").value("JOY"))
                .andExpect(jsonPath("$.total_elements").value(1))
                .andExpect(jsonPath("$.first").value(true))
                .andExpect(jsonPath("$.last").value(true));
    }

    @Test
    void getHistory_shouldReturnCardsAndDecisions_whenAssistantMessageIncludesThem() throws Exception {
        SuggestedActionResponse action = new SuggestedActionResponse(
                ActivityType.BREATHING,
                4L,
                "Respiracion guiada",
                "Respira lento",
                "/guided-breathing",
                12L
        );
        GeneratedChallengeResponse challenge = new GeneratedChallengeResponse("Reto breve", "Tomate cinco minutos");
        ChatHistoryResponse.Message msg = new ChatHistoryResponse.Message(
                3L,
                MessageRole.ASSISTANT,
                "Te sugiero esto",
                false,
                EmotionType.CALM,
                Instant.parse("2024-01-01T00:02:00Z"),
                action,
                challenge,
                "ACCEPTED",
                "REJECTED"
        );
        ChatHistoryResponse page = new ChatHistoryResponse(List.of(msg), 0, 1, 1, 1, true, true);
        when(listChatHistoryUseCase.execute(any(ChatHistoryRequest.class))).thenReturn(page);

        mockMvc.perform(get("/api/chat/conv-1/messages"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].suggested_action.type").value("BREATHING"))
                .andExpect(jsonPath("$.content[0].suggested_action.emotional_event_id").value(12))
                .andExpect(jsonPath("$.content[0].generated_challenge.title").value("Reto breve"))
                .andExpect(jsonPath("$.content[0].suggested_action_decision").value("accepted"))
                .andExpect(jsonPath("$.content[0].challenge_decision").value("rejected"));
    }

    @Test
    void getHistory_shouldReturn200WithEmptyPage_whenNoMessages() throws Exception {
        ChatHistoryResponse emptyPage = new ChatHistoryResponse(List.of(), 0, 20, 0, 0, true, true);
        when(listChatHistoryUseCase.execute(any(ChatHistoryRequest.class))).thenReturn(emptyPage);

        mockMvc.perform(get("/api/chat/conv-vacia/messages"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isEmpty())
                .andExpect(jsonPath("$.total_elements").value(0));
    }

    @Test
    void getHistory_shouldReturn200WithNullRoleAndEmotion_whenMessageHasNulls() throws Exception {
        ChatHistoryResponse.Message msg = new ChatHistoryResponse.Message(
                2L, null, "mensaje", null, null, Instant.now(), null, null, null, null);
        ChatHistoryResponse page = new ChatHistoryResponse(List.of(msg), 0, 1, 1, 1, true, true);
        when(listChatHistoryUseCase.execute(any(ChatHistoryRequest.class))).thenReturn(page);

        mockMvc.perform(get("/api/chat/conv-1/messages"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].role").doesNotExist())
                .andExpect(jsonPath("$.content[0].detected_emotion").doesNotExist());
    }

    @Test
    void getHistory_shouldUseDefaultPagination_whenNoParamsProvided() throws Exception {
        ChatHistoryResponse emptyPage = new ChatHistoryResponse(List.of(), 0, 20, 0, 0, true, true);
        when(listChatHistoryUseCase.execute(any(ChatHistoryRequest.class))).thenReturn(emptyPage);

        mockMvc.perform(get("/api/chat/conv-1/messages"))
                .andExpect(status().isOk());

        verify(listChatHistoryUseCase).execute(argThat(req ->
                req.page() == 0
                        && req.size() == 20
                        && "conv-1".equals(req.conversationId())
                        && USER_ID.equals(req.userId())));
    }

    @Test
    void getHistory_shouldForwardCustomPageAndSize_whenParamsProvided() throws Exception {
        ChatHistoryResponse emptyPage = new ChatHistoryResponse(List.of(), 2, 5, 0, 0, true, true);
        when(listChatHistoryUseCase.execute(any(ChatHistoryRequest.class))).thenReturn(emptyPage);

        mockMvc.perform(get("/api/chat/conv-1/messages")
                        .param("page", "2")
                        .param("size", "5"))
                .andExpect(status().isOk());

        verify(listChatHistoryUseCase).execute(argThat(req ->
                req.page() == 2
                        && req.size() == 5
                        && "conv-1".equals(req.conversationId())));
    }

    @Test
    void sendAudioMessage_shouldReturn200_whenAudioAndConversationIdAreValid() throws Exception {
        ChatReplyResponse reply = reply("entendí tu mensaje de voz", null, null, false, null);
        when(audioChatUseCase.execute(any(), eq("conv-1"), eq(USER_ID))).thenReturn(reply);
        MockMultipartFile audio = new MockMultipartFile("audio", "recording.webm", "audio/webm", "fake".getBytes());

        mockMvc.perform(multipart("/api/chat/audio")
                        .file(audio)
                        .param("conversationId", "conv-1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.huly_reply").value("entendí tu mensaje de voz"));
    }

    @Test
    void sendAudioMessage_shouldDelegateToAudioChatUseCase_withAuthenticatedUserId() throws Exception {
        when(audioChatUseCase.execute(any(), eq("conv-1"), eq(USER_ID)))
                .thenReturn(reply("ok", null, null, null, null));
        MockMultipartFile audio = new MockMultipartFile("audio", "recording.webm", "audio/webm", "fake".getBytes());

        mockMvc.perform(multipart("/api/chat/audio")
                        .file(audio)
                        .param("conversationId", "conv-1"))
                .andExpect(status().isOk());

        verify(audioChatUseCase).execute(any(), eq("conv-1"), eq(USER_ID));
    }

    @Test
    void sendAudioMessage_shouldReturn500_whenConversationIdIsMissing() throws Exception {
        // MissingServletRequestParameterException no está mapeada en GlobalExceptionHandler
        // (solo MethodArgumentNotValidException lo está), cae al catch-all → 500
        MockMultipartFile audio = new MockMultipartFile("audio", "recording.webm", "audio/webm", "fake".getBytes());

        mockMvc.perform(multipart("/api/chat/audio")
                        .file(audio))
                .andExpect(status().isInternalServerError());
    }

    @Test
    void getHistory_shouldReturnPaginationMetadata() throws Exception {
        ChatHistoryResponse.Message msg = new ChatHistoryResponse.Message(
                1L, MessageRole.ASSISTANT, "resp", false, EmotionType.CALM, Instant.now(), null, null, null, null);
        ChatHistoryResponse page = new ChatHistoryResponse(List.of(msg), 0, 1, 1, 1, true, true);
        when(listChatHistoryUseCase.execute(any(ChatHistoryRequest.class))).thenReturn(page);

        mockMvc.perform(get("/api/chat/conv-1/messages"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.page_number").value(0))
                .andExpect(jsonPath("$.page_size").value(1))
                .andExpect(jsonPath("$.total_elements").value(1))
                .andExpect(jsonPath("$.total_pages").value(1));
    }

    @Test
    void challengeDecision_shouldReturn204AndDelegateToSaveChallengeDecisionUseCase() throws Exception {
        com.huly.backend.infrastructure.presentation.dto.chat.ChatChallengeDecisionRequest req =
                new com.huly.backend.infrastructure.presentation.dto.chat.ChatChallengeDecisionRequest("conv-1", "title", "desc", "ACCEPTED");

        mockMvc.perform(post("/api/chat/challenge-decision")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isNoContent());

        verify(saveChallengeDecisionUseCase).execute(eq(USER_ID), eq("title"), eq("ACCEPTED"), eq("desc"), eq("conv-1"));
    }
}
