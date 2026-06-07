package com.huly.backend.presentation.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.huly.backend.domain.model.chat.ChatMessage;
import com.huly.backend.domain.model.chat.ChatReply;
import com.huly.backend.domain.model.chat.ChatStreamEvent;
import com.huly.backend.domain.model.chat.SuggestedChatAction;
import com.huly.backend.domain.model.enums.ActivityType;
import com.huly.backend.domain.model.enums.EmotionType;
import com.huly.backend.domain.model.enums.MessageRole;
import com.huly.backend.domain.service.vector.UserVectorMemoryService;
import com.huly.backend.domain.useCase.chat.ChatUseCase;
import com.huly.backend.domain.useCase.chat.ListChatHistoryUseCase;
import com.huly.backend.domain.useCase.chat.StreamChatUseCase;
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
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import reactor.core.publisher.Flux;

import java.time.Instant;
import java.util.List;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.asyncDispatch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.request;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class ChatControllerTest {

    private MockMvc mockMvc;
    private final ObjectMapper objectMapper = new ObjectMapper();

    private ChatUseCase chatUseCase;
    private ListChatHistoryUseCase listChatHistoryUseCase;
    private StreamChatUseCase streamChatUseCase;
    private UserVectorMemoryService userVectorMemoryService;

    private static final Long USER_ID = 1L;

    @BeforeEach
    void setUp() {
        chatUseCase = mock(ChatUseCase.class);
        listChatHistoryUseCase = mock(ListChatHistoryUseCase.class);
        streamChatUseCase = mock(StreamChatUseCase.class);
        userVectorMemoryService = mock(UserVectorMemoryService.class);

        Authentication authentication = mock(Authentication.class);
        SecurityContext securityContext = mock(SecurityContext.class);
        when(authentication.getName()).thenReturn(String.valueOf(USER_ID));
        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);

        ChatController controller = new ChatController(
                chatUseCase,
                listChatHistoryUseCase,
                streamChatUseCase,
                userVectorMemoryService
        );
        mockMvc = MockMvcBuilders.standaloneSetup(controller)
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
    void stream_shouldEmitSseEvents() throws Exception {
        ChatReply reply = new ChatReply("hola mundo", EmotionType.JOY, 6, false, null);
        when(streamChatUseCase.execute("hola", "conv-1", USER_ID)).thenReturn(Flux.just(
                ChatStreamEvent.delta("hola "),
                ChatStreamEvent.done(reply)
        ));

        MvcResult result = mockMvc.perform(post("/api/chat/stream")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.TEXT_EVENT_STREAM)
                        .content(objectMapper.writeValueAsString(new ChatRequest("hola", "conv-1"))))
                .andExpect(request().asyncStarted())
                .andReturn();

        mockMvc.perform(asyncDispatch(result))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("event:delta")))
                .andExpect(content().string(containsString("event:done")))
                .andExpect(content().string(containsString("hola mundo")));
    }

    @Test
    void stream_shouldEmitSseError_whenConversationIdIsNull() throws Exception {
        MvcResult result = mockMvc.perform(post("/api/chat/stream")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.TEXT_EVENT_STREAM)
                        .content(objectMapper.writeValueAsString(new ChatRequest("hola", null))))
                .andExpect(request().asyncStarted())
                .andReturn();

        mockMvc.perform(asyncDispatch(result))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("event:error")))
                .andExpect(content().string(containsString("message y conversationId son obligatorios.")));

        verifyNoInteractions(streamChatUseCase);
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
}