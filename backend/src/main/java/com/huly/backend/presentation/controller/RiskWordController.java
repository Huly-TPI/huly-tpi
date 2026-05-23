package com.huly.backend.presentation.controller;

import com.huly.backend.domain.model.RiskWord;
import com.huly.backend.domain.useCase.riskWord.CreateRiskWordUseCase;
import com.huly.backend.domain.useCase.riskWord.DeleteRiskWordUseCase;
import com.huly.backend.domain.useCase.riskWord.ListRiskWordsUseCase;
import com.huly.backend.domain.useCase.riskWord.UpdateRiskWordUseCase;
import com.huly.backend.presentation.dto.riskWord.RiskWordPageResponse;
import com.huly.backend.presentation.dto.riskWord.RiskWordRequest;
import com.huly.backend.presentation.dto.riskWord.RiskWordResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * Controlador REST que expone los endpoints de gestión de palabras de riesgo emocional.
 * Cada método delega en su caso de uso correspondiente y aplica el mapeo al DTO
 * de respuesta correspondiente ({@link RiskWordResponse} o {@link RiskWordPageResponse}).
 * El mapeo dominio → DTO es responsabilidad exclusiva de esta capa de presentación.
 *
 * <p>Base URL: {@code /api/risk-words}</p>
 */
@RequiredArgsConstructor
@RestController
@RequestMapping("/api/risk-words")
@PreAuthorize("permitAll()")
//todo descomentar esto cuando este lo de spring security de roles
//@PreAuthorize("hasRole('ADMIN')") // Solo usuarios con rol ADMIN pueden acceder a estos endpoints
public class RiskWordController {

    private final CreateRiskWordUseCase createRiskWordUseCase;
    private final UpdateRiskWordUseCase updateRiskWordUseCase;
    private final DeleteRiskWordUseCase deleteRiskWordUseCase;
    private final ListRiskWordsUseCase listRiskWordsUseCase;

    /**
     * Crea una nueva palabra de riesgo.
     *
     * @param request datos de la palabra a crear, validados automáticamente
     * @return {@code 201 Created} con la palabra creada en el cuerpo
     */
    @PostMapping
    public ResponseEntity<RiskWordResponse> create(@Valid @RequestBody RiskWordRequest request) {
        RiskWord created = createRiskWordUseCase.execute(request.word(), request.description(), request.severity());
        return ResponseEntity.status(HttpStatus.CREATED).body(toResponse(created));
    }

    /**
     * Actualiza una palabra de riesgo existente por su identificador.
     *
     * @param id      identificador de la palabra a actualizar
     * @param request nuevos datos a aplicar, validados automáticamente
     * @return {@code 200 OK} con la palabra actualizada en el cuerpo
     */
    @PutMapping("/{id}")
    public ResponseEntity<RiskWordResponse> update(
            @PathVariable Long id,
            @Valid @RequestBody RiskWordRequest request) {
        RiskWord updated = updateRiskWordUseCase.execute(id, request.word(), request.description(), request.severity());
        return ResponseEntity.ok(toResponse(updated));
    }

    /**
     * Elimina una palabra de riesgo por su identificador.
     *
     * @param id identificador de la palabra a eliminar
     * @return {@code 204 No Content} si la eliminación fue exitosa
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        deleteRiskWordUseCase.execute(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * Lista palabras de riesgo con filtros opcionales y paginación.
     * La respuesta usa {@link RiskWordPageResponse} en lugar de
     * {@link org.springframework.data.domain.Page} para no exponer
     * la API interna de Spring al cliente.
     *
     * @param word     fragmento para filtrar por contenido de la palabra (opcional)
     * @param active   estado activo/inactivo para filtrar (opcional)
     * @param severity nivel de severidad: {@code LOW}, {@code MEDIUM} o {@code HIGH} (opcional)
     * @param page     número de página comenzando en 0 (por defecto {@code 0})
     * @param size     cantidad de elementos por página (por defecto {@code 20})
     * @return {@code 200 OK} con la página de palabras que coinciden con los filtros
     */
    @GetMapping
    public ResponseEntity<RiskWordPageResponse> list(
            @RequestParam(required = false) String word,
            @RequestParam(required = false) Boolean active,
            @RequestParam(required = false) String severity,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<RiskWord> result = listRiskWordsUseCase.execute(word, active, severity, pageable);
        return ResponseEntity.ok(toPageResponse(result));
    }

    /**
     * Convierte un modelo de dominio {@link RiskWord} al DTO de respuesta {@link RiskWordResponse}.
     * La severidad se serializa como texto; si es {@code null} se mantiene como tal.
     *
     * @param riskWord modelo de dominio a convertir
     * @return DTO listo para serializar en la respuesta HTTP
     */
    private RiskWordResponse toResponse(RiskWord riskWord) {
        return new RiskWordResponse(
                riskWord.getId(),
                riskWord.getWord(),
                riskWord.getDescription(),
                riskWord.getSeverity() != null ? riskWord.getSeverity().name() : null,
                riskWord.isActive()
        );
    }

    /**
     * Convierte una {@link Page} de dominio al DTO paginado {@link RiskWordPageResponse}.
     * Aplica {@link #toResponse(RiskWord)} a cada elemento del contenido y extrae
     * los metadatos de paginación necesarios para el cliente.
     *
     * @param page página de modelos de dominio retornada por el caso de uso
     * @return DTO paginado con el contenido mapeado y los metadatos de paginación
     */
    private RiskWordPageResponse toPageResponse(Page<RiskWord> page) {
        return new RiskWordPageResponse(
                page.getContent().stream().map(this::toResponse).toList(),
                page.getNumber(),
                page.getSize(),
                page.getTotalElements(),
                page.getTotalPages(),
                page.isFirst(),
                page.isLast()
        );
    }
}
