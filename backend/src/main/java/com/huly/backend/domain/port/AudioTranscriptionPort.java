package com.huly.backend.domain.port;

public interface AudioTranscriptionPort {

    AudioTranscriptionResult transcribe(byte[] audioBytes, String filename);

    /** Ping liviano al microservicio (GET /health) para mantenerlo despierto. true si respondió OK. */
    boolean ping();

    record VadResult(double arousal, double dominance, double valence) {}

    record AudioTranscriptionResult(
            String transcription,
            VadResult vad,
            String dominantEmotion,
            String language
    ) {}
}
