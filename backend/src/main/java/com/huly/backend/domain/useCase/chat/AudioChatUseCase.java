package com.huly.backend.domain.useCase.chat;

import com.huly.backend.domain.model.chat.ChatReply;
import com.huly.backend.domain.port.AudioTranscriptionPort;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@RequiredArgsConstructor
public class AudioChatUseCase {

    private static final Logger log = LoggerFactory.getLogger(AudioChatUseCase.class);

    private final AudioTranscriptionPort audioTranscriptionPort;
    private final ChatUseCase chatUseCase;

    public ChatReply execute(MultipartFile audio, String conversationId, Long userId) {
        byte[] audioBytes;
        try {
            audioBytes = audio.getBytes();
        } catch (IOException e) {
            throw new RuntimeException("Error al leer el archivo de audio", e);
        }

        String filename = audio.getOriginalFilename() != null ? audio.getOriginalFilename() : "audio.webm";
        AudioTranscriptionPort.AudioTranscriptionResult result = audioTranscriptionPort.transcribe(audioBytes, filename);

        log.info("[AUDIO] transcripción='{}' | emoción='{}' | vad={} | idioma={}",
                result.transcription(), result.dominantEmotion(), result.vad(), result.language());

        String formattedMessage = buildMessage(result.transcription(), result.vad(), result.dominantEmotion());
        log.info("[AUDIO] mensaje enviado a Claude:\n{}", formattedMessage);

        return chatUseCase.execute(formattedMessage, conversationId, userId);
    }

    private String buildMessage(String transcription, AudioTranscriptionPort.VadResult vad, String dominantEmotion) {
        if (transcription == null || transcription.isBlank()) {
            return "[Mensaje de voz del usuario sin contenido reconocible]";
        }

        if (vad != null) {
            // Solo arousal: es un feature acústico puro (energía, ritmo, intensidad)
            // independiente del idioma. Valence y dominance son poco fiables en español.
            double arousal = vad.arousal();
            String arousalLabel = arousal >= 0.67 ? "tensa/agitada"
                                : arousal >= 0.34 ? "con cierta inquietud"
                                :                   "tranquila/serena";
            return String.format(
                    "[Mensaje de voz transcrito]\n" +
                    "Transcripción: %s\n" +
                    "Tono de voz: %s",
                    transcription, arousalLabel
            );
        }

        return "[Mensaje de voz transcrito]\nTranscripción: " + transcription;
    }
}
