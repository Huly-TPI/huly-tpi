package com.huly.backend.domain.model;

import com.huly.backend.domain.model.enums.Mood;
import lombok.*;

import java.time.Instant;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class JournalEntry {
    private Long id;
    private Long userId;
    private Long journalId;
    private String content;
    private Mood mood;
    private Instant createdAt;
}
