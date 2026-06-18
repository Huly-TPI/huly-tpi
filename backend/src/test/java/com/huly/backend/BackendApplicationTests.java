package com.huly.backend;

import com.huly.backend.domain.provider.LLMChatPort;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ActiveProfilesResolver;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

@SpringBootTest
@ActiveProfiles(resolver = BackendApplicationTests.EnvAcitveProfileResolver.class)
class BackendApplicationTests {

    @MockitoBean
    private LLMChatPort llmChatPort;

    @Test
    void contextLoads() {
    }

    static class EnvAcitveProfileResolver implements ActiveProfilesResolver{

        @Override
        public String[] resolve(Class<?> testClass) {
            String activeProfile = System.getProperty("spring.profiles.active");
            if(activeProfile == null){
                activeProfile = System.getenv("SPRING_PROFILES_ACTIVE");
            }
            return new String[] { activeProfile != null ? activeProfile : "h2-test" };
        }
    }
}
