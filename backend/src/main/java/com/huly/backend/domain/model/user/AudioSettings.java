package com.huly.backend.domain.model.user;

public record AudioSettings(
        Double interfaceVolume,
        Double ambientVolume,
        Double minigameVolume
) {
    public static AudioSettings defaults() {
        return new AudioSettings(0.7, 0.1, 1.0);
    }
}
