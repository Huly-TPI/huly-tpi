package com.huly.backend;

import java.io.IOException;
import java.nio.file.*;
import java.util.List;

public class GenerateEntities {

    private static final String PACKAGE_NAME =
            "com.huly.backend.infrastructure.repository.entity";

    private static final Path OUTPUT_DIR =
            Paths.get("F:/proyectos visualStudio/huly-tpi/backend/src/main/java/com/huly/backend/infrastructure/repository/entity");

    public static void main(String[] args) throws IOException {
        Files.createDirectories(OUTPUT_DIR);

        generate("AppUser", "app_user", List.of(
                field("Long", "id", "id", true),
                field("String", "password", "password", false),
                field("String", "email", "email", false),
                field("String", "role", "role", false),
                field("String", "status", "status", false)
        ));

        generate("UserDetail", "user_detail", List.of(
                field("Long", "id", "id", true),
                field("Long", "idAppUser", "id_app_user", false),
                field("String", "name", "name", false),
                field("String", "lastname", "lastname", false),
                field("String", "avatarUrl", "avatar_url", false),
                field("LocalDate", "birth", "birth", false),
                field("Boolean", "onBoardingCompleted", "on_boarding_completed", false),
                field("Boolean", "profileOnBoardingCompleted", "profile_on_boarding_completed", false),
                field("String", "avatarUrl2", "avatar_url_2", false),
                field("OffsetDateTime", "lastLoginDate", "last_login_date", false),
                field("OffsetDateTime", "createdAt", "created_at", false)
        ));

        generate("RefreshToken", "refresh_token", List.of(
                field("Long", "id", "id", true),
                field("Long", "idAppUser", "id_app_user", false),
                field("OffsetDateTime", "createdAt", "created_at", false),
                field("OffsetDateTime", "expiredAt", "expired_at", false),
                field("String", "ipAddress", "ip_address", false)
        ));

        generate("UserSetting", "user_setting", List.of(
                field("Long", "id", "id", true),
                field("Long", "idAppUser", "id_app_user", false),
                field("Integer", "musicVolume", "music_volume", false),
                field("Integer", "effectVolume", "effect_volume", false),
                field("Boolean", "antiScrollEnabled", "anti_scroll_enabled", false),
                field("Boolean", "darkmode", "darkmode", false)
        ));

        generate("UserGoals", "user_goals", List.of(
                field("Long", "id", "id", true),
                field("Long", "idAppUser", "id_app_user", false),
                field("String", "title", "title", false),
                field("String", "description", "description", false),
                field("String", "status", "status", false),
                field("OffsetDateTime", "createdAt", "created_at", false)
        ));

        generate("BreathingTechniques", "breathing_techniques", List.of(
                field("Long", "id", "id", true),
                field("String", "name", "name", false),
                field("String", "description", "description", false),
                field("Integer", "inhaleSeconds", "inhale_seconds", false),
                field("Integer", "holdSeconds", "hold_seconds", false),
                field("Integer", "exhaleSeconds", "exhale_seconds", false),
                field("Integer", "roundsInterval", "rounds_interval", false),
                field("Integer", "rounds", "rounds", false)
        ));

        generate("BreathingSessions", "breathing_sessions", List.of(
                field("Long", "id", "id", true),
                field("Long", "techniqueId", "technique_id", false),
                field("Long", "idAppUser", "id_app_user", false),
                field("OffsetDateTime", "createdAt", "created_at", false)
        ));

        generate("Journal", "journal", List.of(
                field("Long", "id", "id", true),
                field("Long", "idAppUser", "id_app_user", false),
                field("String", "mood", "mood", false),
                field("String", "title", "title", false)
        ));

        generate("JournalEntries", "journal_entries", List.of(
                field("Long", "id", "id", true),
                field("Long", "idJournal", "id_journal", false),
                field("String", "mood", "mood", false),
                field("String", "title", "title", false),
                field("String", "content", "content", false),
                field("OffsetDateTime", "createdAt", "created_at", false)
        ));

        generate("ChatSession", "chat_session", List.of(
                field("Long", "id", "id", true),
                field("Long", "idAppUser", "id_app_user", false),
                field("OffsetDateTime", "startAt", "start_at", false),
                field("OffsetDateTime", "endAt", "end_at", false)
        ));

        generate("ChatMessage", "chat_message", List.of(
                field("Long", "id", "id", true),
                field("Long", "idChatSession", "id_chat_session", false),
                field("String", "message", "message", false),
                field("String", "content", "content", false),
                field("Boolean", "riskDetected", "risk_detected", false),
                field("OffsetDateTime", "createdAt", "created_at", false)
        ));

        generate("Emotion", "emotion", List.of(
                field("Long", "id", "id", true),
                field("Long", "idChatMessage", "id_chat_message", false),
                field("String", "emotionDetected", "emotion_detected", false)
        ));

        generate("ChatConfig", "chat_config", List.of(
                field("Long", "id", "id", true),
                field("Boolean", "riskDetectionEnabled", "risk_detection_enabled", false),
                field("String", "systemPrompt", "system_prompt", false)
        ));

        System.out.println("Entities generadas correctamente en:");
        System.out.println(OUTPUT_DIR.toAbsolutePath());
    }

    private static Field field(String type, String name, String column, boolean id) {
        return new Field(type, name, column, id);
    }

    private static void generate(String className, String tableName, List<Field> fields) throws IOException {
        StringBuilder sb = new StringBuilder();

        sb.append("package ").append(PACKAGE_NAME).append(";\n\n");
        sb.append("import jakarta.persistence.*;\n");
        sb.append("import lombok.*;\n\n");
        sb.append("import java.time.LocalDate;\n");
        sb.append("import java.time.OffsetDateTime;\n");
        sb.append("import java.util.List;\n\n");

        sb.append("@Entity\n");
        sb.append("@Table(name = \"").append(tableName).append("\")\n");
        sb.append("@Getter\n");
        sb.append("@Setter\n");
        sb.append("@NoArgsConstructor\n");
        sb.append("@AllArgsConstructor\n");
        sb.append("@Builder\n");
        sb.append("public class ").append(className).append(" {\n\n");

        for (Field f : fields) {
            if (f.id()) {
                sb.append("    @Id\n");
                sb.append("    @GeneratedValue(strategy = GenerationType.IDENTITY)\n");
                sb.append("    @Column(name = \"").append(f.column()).append("\")\n");
                sb.append("    private ").append(f.type()).append(" ").append(f.name()).append(";\n\n");
                continue;
            }

            if (isFk(f.column())) {
                Relation relation = relationFor(f.column());

                sb.append("    @ManyToOne(fetch = FetchType.LAZY)\n");
                sb.append("    @JoinColumn(name = \"").append(f.column()).append("\")\n");
                sb.append("    private ").append(relation.targetClass()).append(" ").append(relation.fieldName()).append(";\n\n");
                continue;
            }

            sb.append("    @Column(name = \"").append(f.column()).append("\")\n");
            sb.append("    private ").append(f.type()).append(" ").append(f.name()).append(";\n\n");
        }

        for (OneToManyRelation relation : oneToManyFor(className)) {
            sb.append("    @OneToMany(mappedBy = \"").append(relation.mappedBy()).append("\", cascade = CascadeType.ALL, orphanRemoval = true)\n");
            sb.append("    private List<").append(relation.targetClass()).append("> ").append(relation.fieldName()).append(";\n\n");
        }

        sb.append("}\n");

        Files.writeString(
                OUTPUT_DIR.resolve(className + ".java"),
                sb.toString(),
                StandardOpenOption.CREATE,
                StandardOpenOption.TRUNCATE_EXISTING
        );
    }

    private static boolean isFk(String column) {
        return column.equals("id_app_user")
                || column.equals("technique_id")
                || column.equals("id_journal")
                || column.equals("id_chat_session")
                || column.equals("id_chat_message");
    }

    private static Relation relationFor(String column) {
        return switch (column) {
            case "id_app_user" -> new Relation("AppUser", "appUser");
            case "technique_id" -> new Relation("BreathingTechniques", "breathingTechnique");
            case "id_journal" -> new Relation("Journal", "journal");
            case "id_chat_session" -> new Relation("ChatSession", "chatSession");
            case "id_chat_message" -> new Relation("ChatMessage", "chatMessage");
            default -> throw new IllegalArgumentException("FK no soportada: " + column);
        };
    }

    private static List<OneToManyRelation> oneToManyFor(String className) {
        return switch (className) {
            case "AppUser" -> List.of(
                    new OneToManyRelation("UserDetail", "userDetails", "appUser"),
                    new OneToManyRelation("RefreshToken", "refreshTokens", "appUser"),
                    new OneToManyRelation("UserSetting", "userSettings", "appUser"),
                    new OneToManyRelation("UserGoals", "userGoals", "appUser"),
                    new OneToManyRelation("BreathingSessions", "breathingSessions", "appUser"),
                    new OneToManyRelation("Journal", "journals", "appUser"),
                    new OneToManyRelation("ChatSession", "chatSessions", "appUser")
            );
            case "BreathingTechniques" -> List.of(
                    new OneToManyRelation("BreathingSessions", "breathingSessions", "breathingTechnique")
            );
            case "Journal" -> List.of(
                    new OneToManyRelation("JournalEntries", "journalEntries", "journal")
            );
            case "ChatSession" -> List.of(
                    new OneToManyRelation("ChatMessage", "chatMessages", "chatSession")
            );
            case "ChatMessage" -> List.of(
                    new OneToManyRelation("Emotion", "emotions", "chatMessage")
            );
            default -> List.of();
        };
    }

    private record Field(
            String type,
            String name,
            String column,
            boolean id
    ) {}

    private record Relation(
            String targetClass,
            String fieldName
    ) {}

    private record OneToManyRelation(
            String targetClass,
            String fieldName,
            String mappedBy
    ) {}
}
