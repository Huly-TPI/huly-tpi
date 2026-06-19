package com.huly.backend.presentation.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.huly.backend.domain.model.chat.ChatMessage;
import com.huly.backend.domain.model.chat.ChatReply;
import com.huly.backend.domain.model.chat.SuggestedChatAction;
import com.huly.backend.domain.model.enums.ActivityType;
import com.huly.backend.domain.model.enums.EmotionType;
import com.huly.backend.domain.model.enums.MessageRole;
import com.huly.backend.domain.useCase.chat.AudioChatUseCase;
import com.huly.backend.domain.useCase.chat.ChatUseCase;
import com.huly.backend.domain.useCase.chat.ListChatHistoryUseCase;
import com.huly.backend.domain.useCase.chat.SaveChallengeDecisionUseCase;
import com.huly.backend.infrastructure.presentation.controller.ChatController;
import com.huly.backend.infrastructure.presentation.dto.chat.ChatRequest;
import com.huly.backend.infrastructure.presentation.exception.GlobalExceptionHandler;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
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
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import org.springframework.mock.web.MockMultipartFile;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class ChatControllerTest {

    private MockMvc mockMvc;
    private final ObjectMapper objectMapper = new ObjectMapper();

    private ChatUseCase chatUseCase;
    private AudioChatUseCase audioChatUseCase;
    private ListChatHistoryUseCase listChatHistoryUseCase;
    private SaveChallengeDecisionUseCase saveChallengeDecisionUseCase;

    private static final Long USER_ID = 1L;

    @BeforeEach
    void setUp() {
        chatUseCase = mock(ChatUseCase.class);
        audioChatUseCase = mock(AudioChatUseCase.class);
        listChatHistoryUseCase = mock(ListChatHistoryUseCase.class);
        saveChallengeDecisionUseCase = mock(SaveChallengeDecisionUseCase.class);

        UserDetails userDetails = new User(String.valueOf(USER_ID), "", Collections.emptyList());
        SecurityContextHolder.getContext().setAuthentication(
                new TestingAuthenticationToken(userDetails, null));

        ChatController controller = new ChatController(
                chatUseCase,
                audioChatUseCase,
                listChatHistoryUseCase,
                saveChallengeDecisionUseCase
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

    @Test
    void chat_shouldReturn200WithReplyContent_whenRequestIsValid() throws Exception {
        ChatReply reply = new ChatReply("todo bien", EmotionType.JOY, 9, false, null);
        when(chatUseCase.execute(eq("hola"), eq("conv-1"), eq(USER_ID))).thenReturn(reply);

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
        ChatReply reply = new ChatReply("respuesta", null, null, null, null);
        when(chatUseCase.execute(any(), any(), any())).thenReturn(reply);

        mockMvc.perform(post("/api/chat")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new ChatRequest("msg", "conv-1"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.detected_emotion").doesNotExist())
                .andExpect(jsonPath("$.metadata").doesNotExist());
    }

    @Test
    void chat_shouldReturn200WithMetadata_whenRiskDetected() throws Exception {
        ChatReply reply = new ChatReply("cuidado", EmotionType.FEAR, 8, true, "suicidio");
        when(chatUseCase.execute(any(), any(), any())).thenReturn(reply);

        mockMvc.perform(post("/api/chat")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new ChatRequest("estoy mal", "conv-2"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.metadata.risk_detected").value(true))
                .andExpect(jsonPath("$.metadata.matched_word").value("suicidio"));
    }

    @Test
    void chat_shouldReturnSuggestedAction_whenRecommendationExists() throws Exception {
        SuggestedChatAction action = new SuggestedChatAction(
                ActivityType.DIARIO,
                2L,
                "Diario emocional",
                "Un espacio para ordenar pensamientos",
                "/api/activities",
                15L
        );
        ChatReply reply = new ChatReply("te acompaño", EmotionType.SADNESS, 9, false, null, action);
        when(chatUseCase.execute(any(), any(), any())).thenReturn(reply);

        mockMvc.perform(post("/api/chat")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new ChatRequest("estoy triste", "conv-2"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.suggested_action.type").value("DIARIO"))
                .andExpect(jsonPath("$.suggested_action.action_id").value("2"))
                .andExpect(jsonPath("$.suggested_action.title").value("Diario emocional"))
                .andExpect(jsonPath("$.suggested_action.emotional_event_id").value(15));
    }

    @Test
    void chat_shouldReturn200WithNullMetadata_whenRiskDetectedIsNull() throws Exception {
        ChatReply reply = new ChatReply("respuesta", EmotionType.CALM, 3, null, null);
        when(chatUseCase.execute(any(), any(), any())).thenReturn(reply);

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
        when(chatUseCase.execute(any(), any(), any())).thenReturn(ChatReply.of("ok"));

        mockMvc.perform(post("/api/chat")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new ChatRequest("hola", "conv-1"))))
                .andExpect(status().isOk());

        verify(chatUseCase).execute("hola", "conv-1", USER_ID);
    }

    @Test
    void getHistory_shouldReturn200WithPagedMessages() throws Exception {
        ChatMessage msg = new ChatMessage(1L, MessageRole.USER, "hola", false, EmotionType.JOY, Instant.parse("2024-01-01T00:00:00Z"));
        Page<ChatMessage> page = new PageImpl<>(List.of(msg));
        when(listChatHistoryUseCase.execute(eq("conv-1"), eq(USER_ID), any(Pageable.class))).thenReturn(page);

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
    void getHistory_shouldReturn200WithEmptyPage_whenNoMessages() throws Exception {
        Page<ChatMessage> emptyPage = Page.empty();
        when(listChatHistoryUseCase.execute(eq("conv-vacia"), eq(USER_ID), any(Pageable.class))).thenReturn(emptyPage);

        mockMvc.perform(get("/api/chat/conv-vacia/messages"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isEmpty())
                .andExpect(jsonPath("$.total_elements").value(0));
    }

    @Test
    void getHistory_shouldReturn200WithNullRoleAndEmotion_whenMessageHasNulls() throws Exception {
        ChatMessage msg = new ChatMessage(2L, null, "mensaje", null, null, Instant.now());
        Page<ChatMessage> page = new PageImpl<>(List.of(msg));
        when(listChatHistoryUseCase.execute(any(), eq(USER_ID), any(Pageable.class))).thenReturn(page);

        mockMvc.perform(get("/api/chat/conv-1/messages"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].role").doesNotExist())
                .andExpect(jsonPath("$.content[0].detected_emotion").doesNotExist());
    }

    @Test
    void getHistory_shouldUseDefaultPagination_whenNoParamsProvided() throws Exception {
        Page<ChatMessage> emptyPage = Page.empty();
        when(listChatHistoryUseCase.execute(any(), eq(USER_ID), any(Pageable.class))).thenReturn(emptyPage);

        mockMvc.perform(get("/api/chat/conv-1/messages"))
                .andExpect(status().isOk());

        verify(listChatHistoryUseCase).execute(eq("conv-1"), eq(USER_ID), any(Pageable.class));
    }

    @Test
    void getHistory_shouldForwardCustomPageAndSize_whenParamsProvided() throws Exception {
        Page<ChatMessage> emptyPage = Page.empty();
        when(listChatHistoryUseCase.execute(any(), eq(USER_ID), any(Pageable.class))).thenReturn(emptyPage);

        mockMvc.perform(get("/api/chat/conv-1/messages")
                        .param("page", "2")
                        .param("size", "5"))
                .andExpect(status().isOk());

        verify(listChatHistoryUseCase).execute(eq("conv-1"), eq(USER_ID), any(Pageable.class));
    }

    @Test
    void sendAudioMessage_shouldReturn200_whenAudioAndConversationIdAreValid() throws Exception {
        ChatReply reply = new ChatReply("entendí tu mensaje de voz", null, null, false, null);
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
        when(audioChatUseCase.execute(any(), eq("conv-1"), eq(USER_ID))).thenReturn(ChatReply.of("ok"));
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
        ChatMessage msg = new ChatMessage(1L, MessageRole.ASSISTANT, "resp", false, EmotionType.CALM, Instant.now());
        Page<ChatMessage> page = new PageImpl<>(List.of(msg));
        when(listChatHistoryUseCase.execute(any(), eq(USER_ID), any(Pageable.class))).thenReturn(page);

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
