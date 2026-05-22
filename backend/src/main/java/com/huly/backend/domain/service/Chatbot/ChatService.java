package com.huly.backend.domain.service.Chatbot;

import com.huly.backend.presentation.dto.ChatResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ChatService {

    private final ChatClient chatClient;
    private final BotConfigService botConfigService;

//    public ChatResponse chat(String message) {
//
//        String content = "Tu nombre es Huly, responde siempre de forma amable ";
//
//        var converter = new BeanOutputConverter<>(ChatResponse.class);
//        String format = converter.getFormat();
//
//        String answer = chatClient.prompt()
//                .system(content + "\n\n" + format)
//                .user(message)
//                .call()
//                .content();
//
//        return converter.convert(answer);
//    }

    public ChatResponse chat(String message, String conversationId) {

        String instructionsIA = botConfigService.getConfig().getSystemPrompt();

        String answer = chatClient.prompt()
                .system(instructionsIA)
                .advisors(a -> a.param(ChatMemory.CONVERSATION_ID, conversationId))
                .user(message)
                .call()
                .content();

        return new ChatResponse(answer, null, null, null, null, null);
    }

}
