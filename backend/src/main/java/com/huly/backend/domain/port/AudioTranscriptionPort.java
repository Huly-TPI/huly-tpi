package com.huly.backend.domain.port;

public interface AudioTranscriptionPort {

    AudioTranscriptionResult transcribe(byte[] audioBytes, String filename);

    record VadResult(double arousal, double dominance, double valence) {}

    record AudioTranscriptionResult(
            String transcription,
            VadResult vad,
            String dominantEmotion,
            String language
    ) {}
}
