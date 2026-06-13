package com.huly.backend;

import com.huly.backend.domain.provider.LLMChatPort;
import com.huly.backend.domain.provider.StreamingLLMChatPort;

import nl.martijndwars.webpush.PushService;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

@SpringBootTest
@ActiveProfiles("h2-test")
class BackendApplicationTests {

    @MockitoBean
    private LLMChatPort llmChatPort;

    @MockitoBean
    private StreamingLLMChatPort streamingLLMChatPort;

    @MockitoBean
    private PushService pushService;

    @Test
    void contextLoads() {
    }
}
