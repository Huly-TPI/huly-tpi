package com.huly.backend.domain.service.chat;

import com.huly.backend.domain.model.enums.CommunicationStyle;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ChatPreferenceDetectionServiceTest {

    private final ChatPreferenceDetectionService service = new ChatPreferenceDetectionService();

    @Test
    void detectPreferredName_shouldAcceptStandaloneNameDuringOnboarding() {
        assertThat(service.detectPreferredName("sergito", true)).contains("Sergito");
    }

    @Test
    void detectPreferredName_shouldExtractExplicitNameChange() {
        assertThat(service.detectPreferredName(
                "No me digas más Sergio, ahora decime Checho",
                false)).contains("Checho");
    }

    @Test
    void detectPreferredName_shouldPreferAliasAndNeverUseGreetingAsName() {
        assertThat(service.detectPreferredName(
                "Boa tarde, mi nombre es Sergio pero me podes decir crack",
                true)).contains("Crack");
    }

    @Test
    void detectPreferredName_shouldRejectStandalonePortugueseGreeting() {
        assertThat(service.detectPreferredName("Boa tarde", true)).isEmpty();
    }

    @Test
    void detectPreferredName_shouldExtractDeclaredNameWhenNoAliasExists() {
        assertThat(service.detectPreferredName(
                "Hola, mi nombre es Sergio",
                true)).contains("Sergio");
    }

    @Test
    void detectPreferredName_shouldNotTreatOrdinaryQuestionAsNameChange() {
        assertThat(service.detectPreferredName("Decime qué pensás", false)).isEmpty();
    }

    @Test
    void detectCommunicationStyle_shouldAcceptStandaloneStyleDuringOnboarding() {
        assertThat(service.detectCommunicationStyle("informal", true))
                .contains(CommunicationStyle.INFORMAL);
    }

    @Test
    void detectCommunicationStyle_shouldDetectExplicitDirectStyleChange() {
        assertThat(service.detectCommunicationStyle("Respondeme más corto y directo", false))
                .contains(CommunicationStyle.CONCISE_DIRECT);
    }

    @Test
    void detectCommunicationStyle_shouldDistinguishIndirectFromDirect() {
        assertThat(service.detectCommunicationStyle("Quiero que me hables indirecto", false))
                .contains(CommunicationStyle.INDIRECT);
    }

    @Test
    void detectCommunicationStyle_shouldSupportFormalStyle() {
        assertThat(service.detectCommunicationStyle("formal", true))
                .contains(CommunicationStyle.FORMAL);
    }

    @Test
    void detectCommunicationStyle_shouldDetectWantingFriendLikeCommunication() {
        assertThat(service.detectCommunicationStyle(
                "Quiero que me hables como un amigo",
                false)).contains(CommunicationStyle.FRIEND_LIKE);
    }

    @Test
    void detectCommunicationStyle_shouldTreatLessSeriousAsInformal() {
        assertThat(service.detectCommunicationStyle("No seas tan serio", false))
                .contains(CommunicationStyle.INFORMAL);
    }

    @Test
    void detectCommunicationStyle_shouldIgnoreCasualStyleMentions() {
        assertThat(service.detectCommunicationStyle(
                "Mi amigo es muy directo cuando habla",
                false)).isEmpty();
    }
}
