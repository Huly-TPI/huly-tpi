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

/**
 * Controlador del diario emocional.
 * Permite al usuario registrar entradas de diario con su estado de ánimo (mood)
 * y un contenido en formato JSON estructurado con campos como "adentro", "pensamiento", "bien", "manana".
 *
 * Cada entrada creada se indexa en la memoria vectorial del usuario,
 * lo que permite al chatbot recordar y contextualizar lo que el usuario escribió en el diario.
 */
@RestController
@RequestMapping("/api/journal")
@RequiredArgsConstructor
public class JournalController {

    private final CreateJournalEntryUseCase createJournalEntryUseCase;
    private final ListJournalEntriesUseCase listJournalEntriesUseCase;
    private final AppUserRepository appUserRepository;

    /** Lista todas las entradas del diario del usuario autenticado, ordenadas por fecha. */
    @GetMapping
    public ResponseEntity<List<JournalEntryResponse>> list() {
        // El email del usuario autenticado lo provee Spring Security desde el JWT
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        AppUserEntity user = appUserRepository.findByEmail(email)
                .orElseThrow(() -> new NotFoundException("Usuario no encontrado"));

        List<JournalEntryResponse> entries = listJournalEntriesUseCase.execute(user.getId())
                .stream()
                .map(this::toResponse)
                .toList();

        return ResponseEntity.ok(entries);
    }

    /**
     * Crea una nueva entrada de diario emocional para el usuario autenticado.
     * El mood se parsea del string recibido (case-insensitive) al enum Mood.
     * Si useTextForAI=false, el contenido textual NO se indexa en la memoria vectorial
     * (solo se guarda el estado de ánimo como contexto mínimo para la IA).
     */
    @PostMapping
    public ResponseEntity<JournalEntryResponse> create(@Valid @RequestBody JournalEntryRequest request) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        AppUserEntity user = appUserRepository.findByEmail(email)
                .orElseThrow(() -> new NotFoundException("Usuario no encontrado"));

        // Convierte el string de mood al enum, lanzando 400 si el valor es inválido
        Mood mood = null;
        if (request.mood() != null && !request.mood().isBlank()) {
            try {
                mood = Mood.valueOf(request.mood().toUpperCase());
            } catch (IllegalArgumentException e) {
                throw new BadRequestException("Mood inválido: " + request.mood());
            }
        }

        // Por defecto, el texto del diario SÍ se usa para entrenar la memoria de IA
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
