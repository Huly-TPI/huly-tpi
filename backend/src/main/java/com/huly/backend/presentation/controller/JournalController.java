package com.huly.backend.presentation.controller;

import com.huly.backend.domain.model.JournalEntry;
import com.huly.backend.domain.model.enums.Mood;
import com.huly.backend.domain.useCase.journal.CreateJournalEntryUseCase;
import com.huly.backend.domain.useCase.journal.ListJournalEntriesUseCase;
import com.huly.backend.exception.BadRequestException;
import com.huly.backend.exception.NotFoundException;
import com.huly.backend.infrastructure.repository.entity.AppUserEntity;
import com.huly.backend.infrastructure.repository.jpaRepository.interfaces.AppUserRepository;
import com.huly.backend.presentation.dto.journal.JournalEntryRequest;
import com.huly.backend.presentation.dto.journal.JournalEntryResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/journal")
@RequiredArgsConstructor
public class JournalController {

    private final CreateJournalEntryUseCase createJournalEntryUseCase;
    private final ListJournalEntriesUseCase listJournalEntriesUseCase;
    private final AppUserRepository appUserRepository;

    @GetMapping
    public ResponseEntity<List<JournalEntryResponse>> list() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        AppUserEntity user = appUserRepository.findByEmail(email)
                .orElseThrow(() -> new NotFoundException("Usuario no encontrado"));

        List<JournalEntryResponse> entries = listJournalEntriesUseCase.execute(user.getId())
                .stream()
                .map(this::toResponse)
                .toList();

        return ResponseEntity.ok(entries);
    }

    @PostMapping
    public ResponseEntity<JournalEntryResponse> create(@Valid @RequestBody JournalEntryRequest request) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        AppUserEntity user = appUserRepository.findByEmail(email)
                .orElseThrow(() -> new NotFoundException("Usuario no encontrado"));

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
                user.getId(),
                request.content(),
                mood,
                useTextForAI
        );

        return ResponseEntity.status(HttpStatus.CREATED).body(toResponse(entry));
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
