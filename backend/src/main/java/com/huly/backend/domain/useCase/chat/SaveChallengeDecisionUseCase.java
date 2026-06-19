package com.huly.backend.domain.useCase.chat;

import com.huly.backend.domain.model.vector.SaveVectorMemoryCommand;
import com.huly.backend.domain.model.vector.VectorMemorySource;
import com.huly.backend.domain.service.vector.UserVectorMemoryService;
import lombok.RequiredArgsConstructor;
import java.util.Map;

@RequiredArgsConstructor
public class SaveChallengeDecisionUseCase {

    private final UserVectorMemoryService userVectorMemoryService;

    public void execute(Long userId, String title, String decision, String description, String conversationId) {
        if (userId == null || title == null || title.isBlank() || decision == null || decision.isBlank()) {
            return;
        }

        String normalizedDecision = decision.toUpperCase();
        String decText = "ACCEPTED".equals(normalizedDecision) ? "acepto" : "rechazo";
        String desc = description != null ? description : "";
        String content = "El usuario %s el reto: %s. Descripcion: %s."
                .formatted(decText, title, desc);
        String convId = conversationId != null && !conversationId.isBlank() ? conversationId.strip() : "unknown";
        String srcId = String.join(":", "challenge-decision", convId, title.strip(), normalizedDecision);

        userVectorMemoryService.saveMemory(new SaveVectorMemoryCommand(
                userId,
                VectorMemorySource.CHATBOT,
                srcId,
                "CHALLENGE_DECISION",
                "CHALLENGE_DECISION",
                content,
                conversationId,
                null,
                Map.of(
                        "createdFrom", "USER_MESSAGE",
                        "feature", "CHATBOT_CHALLENGE_DECISION",
                        "decision", normalizedDecision,
                        "challengeTitle", title,
                        "challengeDescription", desc
                )
        ));
    }
}
