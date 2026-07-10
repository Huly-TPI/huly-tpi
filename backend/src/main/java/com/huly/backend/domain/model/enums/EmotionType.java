package com.huly.backend.domain.model.enums;

public enum EmotionType {
    // Emociones básicas
    JOY("Alegría", 0.90, 0.60, 0.80),
    SADNESS("Tristeza", -0.85, 0.35, -0.75),
    ANGER("Enojo / Rabia", -0.70, 0.95, -0.40),
    FEAR("Miedo", -0.80, 0.90, -0.85),
    SURPRISE("Sorpresa", 0.30, 0.60, 0.20),
    DISGUST("Disgusto", -0.60, 0.20, -0.30),

    // Estados ansiosos y de estrés
    ANXIETY("Ansiedad", -0.75, 0.85, -0.70),
    STRESS("Estrés", -0.65, 0.75, -0.65),
    OVERWHELM("Abrumación", -0.70, 0.80, -0.75),
    PANIC("Pánico", -0.75, 0.95, -0.90),

    // Estados depresivos
    HOPELESSNESS("Desesperanza", -0.95, 0.15, -0.90),
    EMPTINESS("Vacío", -0.80, -0.20, -0.70),
    LONELINESS("Soledad", -0.90, 0.25, -0.80),
    GRIEF("Duelo", -0.90, 0.40, -0.80),

    // Emociones sociales
    SHAME("Vergüenza", -0.50, 0.30, -0.60),
    GUILT("Culpa", -0.60, 0.35, -0.50),
    FRUSTRATION("Frustración", -0.60, 0.70, -0.50),
    IRRITABILITY("Irritabilidad", -0.55, 0.65, -0.45),

    // Estados positivos / neutros
    CALM("Calma", 0.85, -0.60, 0.70),
    LOVE("Amor", 0.90, 0.50, 0.85),
    GRATITUDE("Gratitud", 0.85, -0.20, 0.75),
    MOTIVATION("Motivación", 0.80, 0.70, 0.80),

    // Estados disociativos o planos
    EXHAUSTION("Agotamiento", -0.60, -0.50, -0.50),
    NUMBNESS("Adormecimiento", -0.40, -0.80, -0.45),
    CONFUSION("Confusión", -0.30, 0.30, -0.35),
    NEUTRAL("Neutral", 0.00, 0.00, 0.00);

    private final String description;
    private final double defaultValence;
    private final double defaultArousal;
    private final double defaultDominance;

    EmotionType(String description, double defaultValence, double defaultArousal, double defaultDominance) {
        this.description = description;
        this.defaultValence = defaultValence;
        this.defaultArousal = defaultArousal;
        this.defaultDominance = defaultDominance;
    }

    public String getDescription() {
        return description;
    }

    public double getDefaultValence() {
        return defaultValence;
    }

    public double getDefaultArousal() {
        return defaultArousal;
    }

    public double getDefaultDominance() {
        return defaultDominance;
    }
}
