package com.huly.backend;

import com.huly.backend.domain.provider.LLMChatPort;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

@SpringBootTest
@ActiveProfiles("coverage-test")
class BackendApplicationTests {

    @MockitoBean
    private LLMChatPort llmChatPort;

    @Test
    void contextLoads() {
    }
}
