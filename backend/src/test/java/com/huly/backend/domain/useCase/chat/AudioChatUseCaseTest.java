package com.huly.backend.domain.useCase.chat;

import com.huly.backend.domain.model.chat.ChatReply;
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

    @Test
    void execute_shouldDelegateToChatUseCaseWithFormattedMessage() throws IOException {
        MultipartFile audio = mockAudio("recording.webm");
        VadResult vad = new VadResult(0.5, 0.5, 0.5);
        AudioTranscriptionResult result = new AudioTranscriptionResult("hola mundo", vad, "neutral", "es");
        when(audioTranscriptionPort.transcribe(any(), eq("recording.webm"))).thenReturn(result);
        when(chatUseCase.execute(any(), eq("conv-1"), eq(1L))).thenReturn(ChatReply.of("respuesta"));

        useCase.execute(audio, "conv-1", 1L);

        verify(chatUseCase).execute(any(), eq("conv-1"), eq(1L));
    }

    @Test
    void execute_shouldIncludeTranscriptionInFormattedMessage() throws IOException {
        MultipartFile audio = mockAudio("recording.webm");
        VadResult vad = new VadResult(0.5, 0.5, 0.5);
        AudioTranscriptionResult result = new AudioTranscriptionResult("texto de prueba", vad, "neutral", "es");
        when(audioTranscriptionPort.transcribe(any(), any())).thenReturn(result);
        when(chatUseCase.execute(any(), any(), any())).thenReturn(ChatReply.of("ok"));

        useCase.execute(audio, "conv-1", 1L);

        verify(chatUseCase).execute(
                org.mockito.ArgumentMatchers.argThat(msg -> msg.contains("texto de prueba")),
                any(),
                any()
        );
    }

    @Test
    void execute_shouldLabelArousal_calmada_whenBelowThreshold() throws IOException {
        MultipartFile audio = mockAudio("audio.webm");
        VadResult vad = new VadResult(0.2, 0.5, 0.5);
        AudioTranscriptionResult result = new AudioTranscriptionResult("algo", vad, "neutral", "es");
        when(audioTranscriptionPort.transcribe(any(), any())).thenReturn(result);
        when(chatUseCase.execute(any(), any(), any())).thenReturn(ChatReply.of("ok"));

        useCase.execute(audio, "conv-1", 1L);

        verify(chatUseCase).execute(
                org.mockito.ArgumentMatchers.argThat(msg -> msg.contains("calmada")),
                any(),
                any()
        );
    }

    @Test
    void execute_shouldLabelArousal_moderadamenteActivada() throws IOException {
        MultipartFile audio = mockAudio("audio.webm");
        VadResult vad = new VadResult(0.5, 0.5, 0.5);
        AudioTranscriptionResult result = new AudioTranscriptionResult("algo", vad, "neutral", "es");
        when(audioTranscriptionPort.transcribe(any(), any())).thenReturn(result);
        when(chatUseCase.execute(any(), any(), any())).thenReturn(ChatReply.of("ok"));

        useCase.execute(audio, "conv-1", 1L);

        verify(chatUseCase).execute(
                org.mockito.ArgumentMatchers.argThat(msg -> msg.contains("moderadamente activada")),
                any(),
                any()
        );
    }

    @Test
    void execute_shouldLabelArousal_muyActivada_whenAboveThreshold() throws IOException {
        MultipartFile audio = mockAudio("audio.webm");
        VadResult vad = new VadResult(0.8, 0.5, 0.5);
        AudioTranscriptionResult result = new AudioTranscriptionResult("algo", vad, "neutral", "es");
        when(audioTranscriptionPort.transcribe(any(), any())).thenReturn(result);
        when(chatUseCase.execute(any(), any(), any())).thenReturn(ChatReply.of("ok"));

        useCase.execute(audio, "conv-1", 1L);

        verify(chatUseCase).execute(
                org.mockito.ArgumentMatchers.argThat(msg -> msg.contains("muy activada")),
                any(),
                any()
        );
    }

    @Test
    void execute_shouldReturnGenericMessage_whenTranscriptionIsBlank() throws IOException {
        MultipartFile audio = mockAudio("audio.webm");
        AudioTranscriptionResult result = new AudioTranscriptionResult("", null, "neutral", "es");
        when(audioTranscriptionPort.transcribe(any(), any())).thenReturn(result);
        when(chatUseCase.execute(any(), any(), any())).thenReturn(ChatReply.of("ok"));

        useCase.execute(audio, "conv-1", 1L);

        verify(chatUseCase).execute(
                org.mockito.ArgumentMatchers.argThat(msg -> msg.contains("sin contenido reconocible")),
                any(),
                any()
        );
    }

    @Test
    void execute_shouldFallbackToAudioWebm_whenFilenameIsNull() throws IOException {
        MultipartFile audio = mock(MultipartFile.class);
        when(audio.getBytes()).thenReturn("bytes".getBytes());
        when(audio.getOriginalFilename()).thenReturn(null);
        AudioTranscriptionResult result = new AudioTranscriptionResult("hola", null, "neutral", "es");
        when(audioTranscriptionPort.transcribe(any(), eq("audio.webm"))).thenReturn(result);
        when(chatUseCase.execute(any(), any(), any())).thenReturn(ChatReply.of("ok"));

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
        ChatReply expected = ChatReply.of("respuesta del bot");
        when(audioTranscriptionPort.transcribe(any(), any())).thenReturn(result);
        when(chatUseCase.execute(any(), any(), any())).thenReturn(expected);

        ChatReply actual = useCase.execute(audio, "conv-1", 1L);

        assertThat(actual).isEqualTo(expected);
    }
}
