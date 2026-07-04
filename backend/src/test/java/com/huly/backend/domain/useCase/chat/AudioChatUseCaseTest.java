package com.huly.backend.domain.useCase.chat;

import com.huly.backend.domain.dto.chat.ChatMessageRequest;
import com.huly.backend.domain.dto.chat.ChatReplyResponse;
import com.huly.backend.domain.port.AudioTranscriptionPort;
import com.huly.backend.domain.port.AudioTranscriptionPort.AudioTranscriptionResult;
import com.huly.backend.domain.port.AudioTranscriptionPort.VadResult;
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

    @Mock
    private AudioTranscriptionPort audioTranscriptionPort;

    @Mock
    private ChatUseCase chatUseCase;

    @InjectMocks
    private AudioChatUseCase useCase;

    private MultipartFile mockAudio(String filename) throws IOException {
        MultipartFile audio = mock(MultipartFile.class);
        when(audio.getBytes()).thenReturn("fake-audio".getBytes());
        when(audio.getOriginalFilename()).thenReturn(filename);
        return audio;
    }

    private ChatReplyResponse reply(String content) {
        return new ChatReplyResponse(content, null, null, null, null, null, null);
    }

    @Test
    void execute_shouldDelegateToChatUseCaseWithFormattedMessage() throws IOException {
        MultipartFile audio = mockAudio("recording.webm");
        VadResult vad = new VadResult(0.5, 0.5, 0.5);
        AudioTranscriptionResult result = new AudioTranscriptionResult("hola mundo", vad, "neutral", "es");
        when(audioTranscriptionPort.transcribe(any(), eq("recording.webm"))).thenReturn(result);
        when(chatUseCase.execute(any(ChatMessageRequest.class))).thenReturn(reply("respuesta"));

        useCase.execute(audio, "conv-1", 1L);

        verify(chatUseCase).execute(argThat(req ->
                "conv-1".equals(req.conversationId()) && req.userId().equals(1L)));
    }

    @Test
    void execute_shouldIncludeTranscriptionInFormattedMessage() throws IOException {
        MultipartFile audio = mockAudio("recording.webm");
        VadResult vad = new VadResult(0.5, 0.5, 0.5);
        AudioTranscriptionResult result = new AudioTranscriptionResult("texto de prueba", vad, "neutral", "es");
        when(audioTranscriptionPort.transcribe(any(), any())).thenReturn(result);
        when(chatUseCase.execute(any(ChatMessageRequest.class))).thenReturn(reply("ok"));

        useCase.execute(audio, "conv-1", 1L);

        verify(chatUseCase).execute(argThat(req -> req.message().contains("texto de prueba")));
    }

    @Test
    void execute_shouldLabelArousal_calmada_whenBelowThreshold() throws IOException {
        MultipartFile audio = mockAudio("audio.webm");
        VadResult vad = new VadResult(0.2, 0.5, 0.5);
        AudioTranscriptionResult result = new AudioTranscriptionResult("algo", vad, "neutral", "es");
        when(audioTranscriptionPort.transcribe(any(), any())).thenReturn(result);
        when(chatUseCase.execute(any(ChatMessageRequest.class))).thenReturn(reply("ok"));

        useCase.execute(audio, "conv-1", 1L);

        verify(chatUseCase).execute(argThat(req -> req.message().contains("tranquila/serena")));
    }

    @Test
    void execute_shouldLabelArousal_moderadamenteActivada() throws IOException {
        MultipartFile audio = mockAudio("audio.webm");
        VadResult vad = new VadResult(0.5, 0.5, 0.5);
        AudioTranscriptionResult result = new AudioTranscriptionResult("algo", vad, "neutral", "es");
        when(audioTranscriptionPort.transcribe(any(), any())).thenReturn(result);
        when(chatUseCase.execute(any(ChatMessageRequest.class))).thenReturn(reply("ok"));

        useCase.execute(audio, "conv-1", 1L);

        verify(chatUseCase).execute(argThat(req -> req.message().contains("con cierta inquietud")));
    }

    @Test
    void execute_shouldLabelArousal_muyActivada_whenAboveThreshold() throws IOException {
        MultipartFile audio = mockAudio("audio.webm");
        VadResult vad = new VadResult(0.8, 0.5, 0.5);
        AudioTranscriptionResult result = new AudioTranscriptionResult("algo", vad, "neutral", "es");
        when(audioTranscriptionPort.transcribe(any(), any())).thenReturn(result);
        when(chatUseCase.execute(any(ChatMessageRequest.class))).thenReturn(reply("ok"));

        useCase.execute(audio, "conv-1", 1L);

        verify(chatUseCase).execute(argThat(req -> req.message().contains("tensa/agitada")));
    }

    @Test
    void execute_shouldReturnGenericMessage_whenTranscriptionIsBlank() throws IOException {
        MultipartFile audio = mockAudio("audio.webm");
        AudioTranscriptionResult result = new AudioTranscriptionResult("", null, "neutral", "es");
        when(audioTranscriptionPort.transcribe(any(), any())).thenReturn(result);
        when(chatUseCase.execute(any(ChatMessageRequest.class))).thenReturn(reply("ok"));

        useCase.execute(audio, "conv-1", 1L);

        verify(chatUseCase).execute(argThat(req -> req.message().contains("sin contenido reconocible")));
    }

    @Test
    void execute_shouldFallbackToAudioWebm_whenFilenameIsNull() throws IOException {
        MultipartFile audio = mock(MultipartFile.class);
        when(audio.getBytes()).thenReturn("bytes".getBytes());
        when(audio.getOriginalFilename()).thenReturn(null);
        AudioTranscriptionResult result = new AudioTranscriptionResult("hola", null, "neutral", "es");
        when(audioTranscriptionPort.transcribe(any(), eq("audio.webm"))).thenReturn(result);
        when(chatUseCase.execute(any(ChatMessageRequest.class))).thenReturn(reply("ok"));

        useCase.execute(audio, "conv-1", 1L);

        verify(audioTranscriptionPort).transcribe(any(), eq("audio.webm"));
    }

    @Test
    void execute_shouldThrowRuntimeException_whenAudioBytesReadFails() throws IOException {
        MultipartFile audio = mock(MultipartFile.class);
        when(audio.getBytes()).thenThrow(new IOException("disco lleno"));

        assertThatThrownBy(() -> useCase.execute(audio, "conv-1", 1L))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Error al leer el archivo de audio");
    }

    @Test
    void execute_shouldReturnChatReplyFromChatUseCase() throws IOException {
        MultipartFile audio = mockAudio("audio.webm");
        VadResult vad = new VadResult(0.4, 0.5, 0.6);
        AudioTranscriptionResult result = new AudioTranscriptionResult("texto", vad, "happy", "es");
        ChatReplyResponse expected = reply("respuesta del bot");
        when(audioTranscriptionPort.transcribe(any(), any())).thenReturn(result);
        when(chatUseCase.execute(any(ChatMessageRequest.class))).thenReturn(expected);

        ChatReplyResponse actual = useCase.execute(audio, "conv-1", 1L);

        assertThat(actual).isEqualTo(expected);
    }
}
