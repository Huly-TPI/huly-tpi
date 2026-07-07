package com.huly.backend.domain.model.enums;

public enum EmotionType {
    // Emociones básicas
    JOY("Alegría"),
    SADNESS("Tristeza"),
    ANGER("Enojo / Rabia"),
    FEAR("Miedo"),
    SURPRISE("Sorpresa"),
    DISGUST("Disgusto"),

    // Estados ansiosos y de estrés
    ANXIETY("Ansiedad"),
    STRESS("Estrés"),
    OVERWHELM("Abrumación"),
    PANIC("Pánico"),

    // Estados depresivos
    HOPELESSNESS("Desesperanza"),
    EMPTINESS("Vacío"),
    LONELINESS("Soledad"),
    GRIEF("Duelo"),

    // Emociones sociales
    SHAME("Vergüenza"),
    GUILT("Culpa"),
    FRUSTRATION("Frustración"),
    IRRITABILITY("Irritabilidad"),

    // Estados positivos / neutros
    CALM("Calma"),
    LOVE("Amor"),
    GRATITUDE("Gratitud"),
    MOTIVATION("Motivación"),

    // Estados disociativos o planos
    EXHAUSTION("Agotamiento"),
    NUMBNESS("Adormecimiento"),
    CONFUSION("Confusión"),
    NEUTRAL("Neutral");

    private final String description;

    EmotionType(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
