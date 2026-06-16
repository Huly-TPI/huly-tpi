package com.huly.backend.domain.service.chat;

import com.huly.backend.domain.model.chat.ChatPreferenceDetectionResult;
import com.huly.backend.domain.model.enums.ChatPreferenceExpectedField;
import com.huly.backend.domain.model.enums.ChatPreferenceMessageType;
import com.huly.backend.domain.provider.ChatPreferenceExtractionPort;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ChatPreferenceResolutionServiceTest {

    @Test
    void resolve_shouldUseSemanticExtractionDuringOnboarding() {
        RecordingExtractionPort port = new RecordingExtractionPort(
                new ChatPreferenceDetectionResult(
                        "Crack",
                        null,
                        ChatPreferenceMessageType.PREFERENCE_ONLY,
                        0.95));
        ChatPreferenceResolutionService service = new ChatPreferenceResolutionService(
                new ChatPreferenceDetectionService(),
                port);

        ChatPreferenceDetectionResult result = service.resolve(
                "Boa tarde, mi nombre es Sergio pero me podes decir crack",
                ChatPreferenceExpectedField.PREFERRED_NAME);

        assertThat(result.preferredName()).isEqualTo("Crack");
        assertThat(result.messageType()).isEqualTo(ChatPreferenceMessageType.PREFERENCE_ONLY);
        assertThat(port.called).isTrue();
    }

    @Test
    void resolve_shouldAcceptValidatedHighConfidenceSemanticName() {
        RecordingExtractionPort port = new RecordingExtractionPort(
                new ChatPreferenceDetectionResult(
                        "Checho",
                        null,
                        ChatPreferenceMessageType.PREFERENCE_ONLY,
                        0.92));
        ChatPreferenceResolutionService service = new ChatPreferenceResolutionService(
                new ChatPreferenceDetectionService(),
                port);

        ChatPreferenceDetectionResult result = service.resolve(
                "Podrías usar mi apodo habitual",
                ChatPreferenceExpectedField.PREFERRED_NAME);

        assertThat(result.preferredName()).isEqualTo("Checho");
        assertThat(port.called).isTrue();
    }

    @Test
    void resolve_shouldRejectLowConfidenceSemanticResult() {
        RecordingExtractionPort port = new RecordingExtractionPort(
                new ChatPreferenceDetectionResult(
                        "Boa tarde",
                        null,
                        ChatPreferenceMessageType.PREFERENCE_ONLY,
                        0.60));
        ChatPreferenceResolutionService service = new ChatPreferenceResolutionService(
                new ChatPreferenceDetectionService(),
                port);

        ChatPreferenceDetectionResult result = service.resolve(
                "Boa tarde, todo bien",
                ChatPreferenceExpectedField.PREFERRED_NAME);

        assertThat(result.hasPreference()).isFalse();
    }

    @Test
    void resolve_shouldNotCallSemanticFallbackAfterOnboarding() {
        RecordingExtractionPort port = new RecordingExtractionPort(
                new ChatPreferenceDetectionResult(
                        "Incorrecto",
                        null,
                        ChatPreferenceMessageType.PREFERENCE_ONLY,
                        1.0));
        ChatPreferenceResolutionService service = new ChatPreferenceResolutionService(
                new ChatPreferenceDetectionService(),
                port);

        ChatPreferenceDetectionResult result = service.resolve(
                "Hoy tuve un día tranquilo",
                ChatPreferenceExpectedField.ANY);

        assertThat(result.hasPreference()).isFalse();
        assertThat(port.called).isFalse();
    }

    @Test
    void resolve_shouldCallSemanticExtractionAfterOnboardingWhenSignalPresent() {
        RecordingExtractionPort port = new RecordingExtractionPort(
                new ChatPreferenceDetectionResult(
                        "Sergio",
                        null,
                        ChatPreferenceMessageType.PREFERENCE_ONLY,
                        0.95));
        ChatPreferenceResolutionService service = new ChatPreferenceResolutionService(
                new ChatPreferenceDetectionService(),
                port);

        ChatPreferenceDetectionResult result = service.resolve(
                "decime Sergio por favor",
                ChatPreferenceExpectedField.ANY);

        assertThat(result.preferredName()).isEqualTo("Sergio");
        assertThat(port.called).isTrue();
    }

    private static final class RecordingExtractionPort implements ChatPreferenceExtractionPort {

        private final ChatPreferenceDetectionResult result;
        private boolean called;

        private RecordingExtractionPort(ChatPreferenceDetectionResult result) {
            this.result = result;
        }

        @Override
        public ChatPreferenceDetectionResult extract(
                String message,
                ChatPreferenceExpectedField expectedField) {
            called = true;
            return result;
        }
    }
}
