package com.huly.backend.infrastructure.presentation.controller;

import com.huly.backend.domain.model.JournalEntry;
import com.huly.backend.domain.model.enums.Mood;
import com.huly.backend.domain.useCase.journal.CreateJournalEntryUseCase;
import com.huly.backend.domain.useCase.journal.ListJournalEntriesUseCase;
import com.huly.backend.infrastructure.presentation.dto.journal.JournalEntryRequest;
import com.huly.backend.infrastructure.presentation.dto.journal.JournalEntryResponse;
import com.huly.backend.infrastructure.presentation.exception.BadRequestException;
import com.huly.backend.infrastructure.presentation.exception.UnauthorizedException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/journal")
@RequiredArgsConstructor
public class JournalController {

    private final CreateJournalEntryUseCase createJournalEntryUseCase;
    private final ListJournalEntriesUseCase listJournalEntriesUseCase;

    @GetMapping
    public ResponseEntity<List<JournalEntryResponse>> list(@AuthenticationPrincipal UserDetails principal) {
        Long userId = getUserId(principal);
        List<JournalEntryResponse> entries = listJournalEntriesUseCase.execute(userId)
                .stream()
                .map(this::toResponse)
                .toList();

        return ResponseEntity.ok(entries);
    }

    @PostMapping
    public ResponseEntity<JournalEntryResponse> create(
            @AuthenticationPrincipal UserDetails principal,
            @Valid @RequestBody JournalEntryRequest request) {
        Long userId = getUserId(principal);

        Mood mood = null;
        if (request.mood() != null && !request.mood().isBlank()) {
            try {
                mood = Mood.valueOf(request.mood().toUpperCase());
            } catch (IllegalArgumentException e) {
                throw new BadRequestException("Mood inválido: " + request.mood());
            }
        }

        boolean useTextForAI = request.useTextForAI() == null || request.useTextForAI();

        JournalEntry entry = createJournalEntryUseCase.execute(
                userId,
                request.content(),
                mood,
                useTextForAI
        );

        return ResponseEntity.status(HttpStatus.CREATED).body(toResponse(entry));
    }

    private Long getUserId(UserDetails principal) {
        if (principal == null) {
            throw new UnauthorizedException("Not authenticated");
        }
        return Long.parseLong(principal.getUsername());
    }

    private JournalEntryResponse toResponse(JournalEntry entry) {
        return new JournalEntryResponse(
                entry.getId(),
                entry.getContent(),
                entry.getMood() != null ? entry.getMood().name() : null,
                entry.getCreatedAt()
        );
    }
}
