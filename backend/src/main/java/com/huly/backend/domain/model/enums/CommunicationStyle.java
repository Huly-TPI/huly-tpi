package com.huly.backend.domain.model.enums;

public enum CommunicationStyle {
    NEUTRAL("neutro", "Usá un tono equilibrado, claro y sin excesiva informalidad."),
    SERIOUS("serio", "Usá un tono serio, sobrio y respetuoso."),
    FORMAL("formal", "Usá un registro formal, respetuoso y cuidado, evitando modismos informales."),
    FRIENDLY("amable", "Usá un tono amable, simpático y acogedor."),
    INFORMAL("informal", "Usá un tono informal y natural, manteniendo el respeto."),
    CLOSE("cercano", "Usá un tono cercano, cálido y personal."),
    FRIEND_LIKE("como un amigo", "Conversá con cercanía y naturalidad, como un amigo de confianza."),
    DIRECT("directo", "Respondé de forma directa, clara y sin rodeos innecesarios."),
    INDIRECT("indirecto", "Respondé de forma gradual y diplomática, evitando formulaciones bruscas."),
    GENTLE_SUPPORTIVE("suave y contenedor", "Usá un tono suave, paciente y emocionalmente contenedor."),
    MOTIVATIONAL("motivador", "Usá un tono motivador, realista y orientado a pequeños avances."),
    CONCISE_DIRECT("corto y directo", "Respondé de forma breve, concreta y directa.");

    public static final String QUESTION_TEXT =
            "¿Cómo te gustaría que te hable? Puede ser de forma neutra, amable, informal, "
                    + "formal, directa, indirecta, cercana o como un amigo.";

    private final String displayName;
    private final String promptInstruction;

    CommunicationStyle(String displayName, String promptInstruction) {
        this.displayName = displayName;
        this.promptInstruction = promptInstruction;
    }

    public String displayName() {
        return displayName;
    }

    public String promptInstruction() {
        return promptInstruction;
    }
}
