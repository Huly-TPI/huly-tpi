package com.huly.backend.infrastructure.adapter;

import com.huly.backend.domain.port.AudioTranscriptionPort;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@Component
public class SenseVoiceAdapter implements AudioTranscriptionPort {

    @Value("${app.sensevoice.url}")
    private String senseVoiceUrl;

    private final RestTemplate restTemplate = new RestTemplate();

    @Override
    public AudioTranscriptionResult transcribe(byte[] audioBytes, String filename) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);

        ByteArrayResource audioResource = new ByteArrayResource(audioBytes) {
            @Override
            public String getFilename() {
                return filename;
            }
        };

        HttpHeaders partHeaders = new HttpHeaders();
        partHeaders.setContentType(MediaType.parseMediaType(resolveContentType(filename)));
        HttpEntity<ByteArrayResource> filePart = new HttpEntity<>(audioResource, partHeaders);

        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        body.add("file", filePart);

        HttpEntity<MultiValueMap<String, Object>> entity = new HttpEntity<>(body, headers);

        try {
            @SuppressWarnings("rawtypes")
            Map responseBody = restTemplate.postForObject(senseVoiceUrl + "/analyze", entity, Map.class);

            if (responseBody == null) {
                return new AudioTranscriptionResult("", null, "neutral", "unknown");
            }

            String transcription   = (String) responseBody.getOrDefault("transcripcion", "");
            String language        = (String) responseBody.getOrDefault("idioma_detectado", "unknown");
            String dominantEmotion = (String) responseBody.getOrDefault("emocion_dominante", "neutral");

            VadResult vad = null;
            Object vadRaw = responseBody.get("vad");
            if (vadRaw instanceof Map) {
                @SuppressWarnings("unchecked")
                Map<String, Object> vadMap = (Map<String, Object>) vadRaw;
                double arousal   = ((Number) vadMap.getOrDefault("arousal",   0.5)).doubleValue();
                double dominance = ((Number) vadMap.getOrDefault("dominance", 0.5)).doubleValue();
                double valence   = ((Number) vadMap.getOrDefault("valence",   0.5)).doubleValue();
                vad = new VadResult(arousal, dominance, valence);
            }

            return new AudioTranscriptionResult(transcription, vad, dominantEmotion, language);

        } catch (Exception e) {
            throw new RuntimeException("No se pudo transcribir el audio: servicio no disponible", e);
        }
    }

    private String resolveContentType(String filename) {
        if (filename == null) return "video/webm";
        String lower = filename.toLowerCase();
        if (lower.endsWith(".wav"))  return "audio/wav";
        if (lower.endsWith(".mp3"))  return "audio/mpeg";
        if (lower.endsWith(".ogg"))  return "audio/ogg";
        if (lower.endsWith(".flac")) return "audio/flac";
        return "video/webm";
    }
}
