package com.huly.backend.domain.model.enums;

public enum ActivityType {
    BREATHING("Respiración guiada"),
    DIARY("Diario emocional"),
    LANTERN("Farolitos de papel"),
    BUBBLE("Burbujas relajantes"),
    CHALLENGE("Reto diario"),
    ZEN_GARDEN("Jardín Zen de arena"),
    MANDALA("Mandalas para colorear"),
    STONES("Piedras del lago"),
    PENDING("Pendientes");

    private final String description;

    ActivityType(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
