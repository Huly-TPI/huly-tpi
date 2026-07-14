package com.huly.backend.infrastructure.adapter;

import com.huly.backend.domain.port.AudioTranscriptionPort;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@Slf4j
@Component
public class SenseVoiceAdapter implements AudioTranscriptionPort {

    private static final int  MAX_RETRIES    = 5;
    private static final long RETRY_DELAY_MS = 5_000L;

    @Value("${app.sensevoice.url}")
    private String senseVoiceUrl;

    private final RestTemplate restTemplate;
    private final RestTemplate healthRestTemplate;

    public SenseVoiceAdapter() {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(10_000);
        factory.setReadTimeout(120_000);
        this.restTemplate = new RestTemplate(factory);

        // Cliente aparte para el keep-alive: timeout corto para no bloquear el hilo del scheduler.
        SimpleClientHttpRequestFactory healthFactory = new SimpleClientHttpRequestFactory();
        healthFactory.setConnectTimeout(5_000);
        healthFactory.setReadTimeout(15_000);
        this.healthRestTemplate = new RestTemplate(healthFactory);
    }

    @Override
    public boolean ping() {
        try {
            ResponseEntity<String> response = healthRestTemplate.getForEntity(senseVoiceUrl + "/health", String.class);
            return response.getStatusCode().is2xxSuccessful();
        } catch (Exception e) {
            log.warn("SenseVoice /health no respondió: {}", e.getMessage());
            return false;
        }
    }

    @Override
    public AudioTranscriptionResult transcribe(byte[] audioBytes, String filename) {
        String[]      transcriptionResult = doWithRetry(() -> doTranscribe(audioBytes, filename));
        EmotionResult emotionResult       = doWithRetry(() -> doAnalyzeEmotion(audioBytes, filename));

        String    transcription   = transcriptionResult[0];
        String    language        = transcriptionResult[1];
        String    dominantEmotion = emotionResult.dominantEmotion();
        VadResult vad             = emotionResult.vad();

        return new AudioTranscriptionResult(transcription, vad, dominantEmotion, language);
    }

    private String[] doTranscribe(byte[] audioBytes, String filename) {
        HttpEntity<MultiValueMap<String, Object>> entity = buildMultipartEntity(audioBytes, filename);

        @SuppressWarnings("rawtypes")
        Map responseBody = restTemplate.postForObject(senseVoiceUrl + "/transcribe", entity, Map.class);

        if (responseBody == null) return new String[]{"", "unknown"};

        String transcripcion = (String) responseBody.getOrDefault("transcripcion", "");
        String idioma        = (String) responseBody.getOrDefault("idioma_detectado", "unknown");
        return new String[]{transcripcion, idioma};
    }

    private EmotionResult doAnalyzeEmotion(byte[] audioBytes, String filename) {
        HttpEntity<MultiValueMap<String, Object>> entity = buildMultipartEntity(audioBytes, filename);

        @SuppressWarnings("rawtypes")
        Map responseBody = restTemplate.postForObject(senseVoiceUrl + "/analyze", entity, Map.class);

        if (responseBody == null) return new EmotionResult("neutral", null);

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

        return new EmotionResult(dominantEmotion, vad);
    }

    private HttpEntity<MultiValueMap<String, Object>> buildMultipartEntity(byte[] audioBytes, String filename) {
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

        return new HttpEntity<>(body, headers);
    }

    @FunctionalInterface
    private interface ThrowingSupplier<T> {
        T get() throws Exception;
    }

    private <T> T doWithRetry(ThrowingSupplier<T> action) {
        Exception lastException = null;
        for (int attempt = 1; attempt <= MAX_RETRIES; attempt++) {
            try {
                return action.get();
            } catch (HttpServerErrorException e) {
                int status = e.getStatusCode().value();
                if ((status == 502 || status == 503) && attempt < MAX_RETRIES) {
                    lastException = e;
                    sleep(RETRY_DELAY_MS);
                } else {
                    throw new RuntimeException("No se pudo transcribir el audio: servicio no disponible", e);
                }
            } catch (ResourceAccessException e) {
                if (attempt < MAX_RETRIES) {
                    lastException = e;
                    sleep(RETRY_DELAY_MS);
                } else {
                    throw new RuntimeException("No se pudo transcribir el audio: servicio no disponible", e);
                }
            } catch (RuntimeException e) {
                throw e;
            } catch (Exception e) {
                throw new RuntimeException("No se pudo transcribir el audio: servicio no disponible", e);
            }
        }
        throw new RuntimeException("No se pudo transcribir el audio: servicio no disponible", lastException);
    }

    private void sleep(long ms) {
        try {
            Thread.sleep(ms);
        } catch (InterruptedException ie) {
            Thread.currentThread().interrupt();
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

    private record EmotionResult(String dominantEmotion, VadResult vad) {}
}
