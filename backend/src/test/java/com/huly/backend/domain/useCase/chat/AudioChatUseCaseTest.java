package com.huly.backend.domain.useCase.chat;

import com.huly.backend.domain.dto.chat.ChatMessageRequest;
import com.huly.backend.domain.dto.chat.ChatReplyResponse;
import com.huly.backend.domain.port.AudioTranscriptionPort;
import com.huly.backend.domain.port.AudioTranscriptionPort.AudioTranscriptionResult;
import com.huly.backend.domain.port.AudioTranscriptionPort.VadResult;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AudioChatUseCaseTest {

    private static final String CONVERSATION_ID = "conv-1";
    private static final Long USER_ID = 1L;

    @Mock
    private AudioTranscriptionPort audioTranscriptionPort;

    @Mock
    private ChatUseCase chatUseCase;

    @InjectMocks
    private AudioChatUseCase useCase;

    private MultipartFile audio;
    private ChatReplyResponse stubbedReply;

    @Test
    @DisplayName("Delega en ChatUseCase con la conversación y el usuario correctos")
    void executeShouldDelegateToChatUseCaseWithFormattedMessage() throws IOException {
        // --- arrange ---
        givenAudioWithFilename("recording.webm");
        givenTranscription("hola mundo", vad(0.5));
        givenChatUseCaseReplies();
        // --- act ---
        execute();
        // --- assert ---
        thenChatUseCaseReceivedConversationAndUser();
    }

    @Test
    @DisplayName("Incluye la transcripción dentro del mensaje formateado")
    void executeShouldIncludeTranscriptionInFormattedMessage() throws IOException {
        // --- arrange ---
        givenAudioWithFilename("recording.webm");
        givenTranscription("texto de prueba", vad(0.5));
        givenChatUseCaseReplies();
        // --- act ---
        execute();
        // --- assert ---
        thenChatUseCaseReceivedMessageContaining("texto de prueba");
    }

    @Test
    @DisplayName("Etiqueta el arousal como tranquila/serena cuando está por debajo del umbral")
    void executeShouldLabelArousalCalmadaWhenBelowThreshold() throws IOException {
        // --- arrange ---
        givenAudioWithFilename("audio.webm");
        givenTranscription("algo", vad(0.2));
        givenChatUseCaseReplies();
        // --- act ---
        execute();
        // --- assert ---
        thenChatUseCaseReceivedMessageContaining("tranquila/serena");
    }

    @Test
    @DisplayName("Etiqueta el arousal como con cierta inquietud en el rango intermedio")
    void executeShouldLabelArousalModeradamenteActivada() throws IOException {
        // --- arrange ---
        givenAudioWithFilename("audio.webm");
        givenTranscription("algo", vad(0.5));
        givenChatUseCaseReplies();
        // --- act ---
        execute();
        // --- assert ---
        thenChatUseCaseReceivedMessageContaining("con cierta inquietud");
    }

    @Test
    @DisplayName("Etiqueta el arousal como tensa/agitada cuando supera el umbral")
    void executeShouldLabelArousalMuyActivadaWhenAboveThreshold() throws IOException {
        // --- arrange ---
        givenAudioWithFilename("audio.webm");
        givenTranscription("algo", vad(0.8));
        givenChatUseCaseReplies();
        // --- act ---
        execute();
        // --- assert ---
        thenChatUseCaseReceivedMessageContaining("tensa/agitada");
    }

    @Test
    @DisplayName("Devuelve un mensaje genérico cuando la transcripción está en blanco")
    void executeShouldReturnGenericMessageWhenTranscriptionIsBlank() throws IOException {
        // --- arrange ---
        givenAudioWithFilename("audio.webm");
        givenTranscription("", null);
        givenChatUseCaseReplies();
        // --- act ---
        execute();
        // --- assert ---
        thenChatUseCaseReceivedMessageContaining("sin contenido reconocible");
    }

    @Test
    @DisplayName("Devuelve un mensaje genérico cuando la transcripción es nula")
    void executeShouldReturnGenericMessageWhenTranscriptionIsNull() throws IOException {
        // --- arrange ---
        givenAudioWithFilename("audio.webm");
        givenTranscription(null, null);
        givenChatUseCaseReplies();
        // --- act ---
        execute();
        // --- assert ---
        thenChatUseCaseReceivedMessageContaining("sin contenido reconocible");
    }

    @Test
    @DisplayName("Usa audio.webm como nombre de archivo cuando el original es nulo")
    void executeShouldFallbackToAudioWebmWhenFilenameIsNull() throws IOException {
        // --- arrange ---
        givenAudioWithFilename(null);
        givenTranscriptionForFilename("audio.webm", "hola", null);
        givenChatUseCaseReplies();
        // --- act ---
        execute();
        // --- assert ---
        thenPortTranscribedWithFilename("audio.webm");
    }

    @Test
    @DisplayName("Lanza RuntimeException cuando falla la lectura de los bytes del audio")
    void executeShouldThrowRuntimeExceptionWhenAudioBytesReadFails() throws IOException {
        // --- arrange ---
        givenAudioBytesReadFails();
        // --- act + assert ---
        thenExecuteThrowsRuntimeWithAudioReadMessage();
    }

    @Test
    @DisplayName("Devuelve la respuesta que produce ChatUseCase")
    void executeShouldReturnChatReplyFromChatUseCase() throws IOException {
        // --- arrange ---
        givenAudioWithFilename("audio.webm");
        givenTranscription("texto", vad(0.4));
        givenChatUseCaseReplies();
        // --- act ---
        ChatReplyResponse actual = execute();
        // --- assert ---
        thenReturnedReplyIsTheOneFromChatUseCase(actual);
    }

    // --- arrange ---

    private void givenAudioWithFilename(String filename) throws IOException {
        audio = mock(MultipartFile.class);
        when(audio.getBytes()).thenReturn("fake-audio".getBytes());
        when(audio.getOriginalFilename()).thenReturn(filename);
    }

    private void givenAudioBytesReadFails() throws IOException {
        audio = mock(MultipartFile.class);
        when(audio.getBytes()).thenThrow(new IOException("disco lleno"));
    }

    private void givenTranscription(String transcription, VadResult vad) {
        AudioTranscriptionResult result = new AudioTranscriptionResult(transcription, vad, "neutral", "es");
        when(audioTranscriptionPort.transcribe(any(), any())).thenReturn(result);
    }

    private void givenTranscriptionForFilename(String filename, String transcription, VadResult vad) {
        AudioTranscriptionResult result = new AudioTranscriptionResult(transcription, vad, "neutral", "es");
        when(audioTranscriptionPort.transcribe(any(), eq(filename))).thenReturn(result);
    }

    private void givenChatUseCaseReplies() {
        stubbedReply = reply("respuesta del bot");
        when(chatUseCase.execute(any(ChatMessageRequest.class))).thenReturn(stubbedReply);
    }

    private VadResult vad(double arousal) {
        return new VadResult(arousal, 0.5, 0.5);
    }

    private ChatReplyResponse reply(String content) {
        return new ChatReplyResponse(content, null, null, null, null, null, null);
    }

    // --- act ---

    private ChatReplyResponse execute() {
        return useCase.execute(audio, CONVERSATION_ID, USER_ID);
    }

    // --- assert ---

    private void thenChatUseCaseReceivedConversationAndUser() {
        verify(chatUseCase).execute(argThat(req ->
                CONVERSATION_ID.equals(req.conversationId()) && USER_ID.equals(req.userId())));
    }

    private void thenChatUseCaseReceivedMessageContaining(String fragment) {
        verify(chatUseCase).execute(argThat(req -> req.message().contains(fragment)));
    }

    private void thenPortTranscribedWithFilename(String filename) {
        verify(audioTranscriptionPort).transcribe(any(), eq(filename));
    }

    private void thenExecuteThrowsRuntimeWithAudioReadMessage() {
        assertThatThrownBy(this::execute)
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Error al leer el archivo de audio");
    }

    private void thenReturnedReplyIsTheOneFromChatUseCase(ChatReplyResponse actual) {
        assertThat(actual).isEqualTo(stubbedReply);
    }
}
