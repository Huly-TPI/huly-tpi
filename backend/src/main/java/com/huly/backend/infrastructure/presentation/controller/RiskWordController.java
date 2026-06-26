package com.huly.backend.infrastructure.presentation.controller;

import com.huly.backend.domain.useCase.riskWord.CreateRiskWordUseCase;
import com.huly.backend.domain.useCase.riskWord.DeleteRiskWordUseCase;
import com.huly.backend.domain.useCase.riskWord.ListRiskWordsUseCase;
import com.huly.backend.domain.useCase.riskWord.UpdateRiskWordUseCase;
import com.huly.backend.infrastructure.presentation.dto.riskWord.RiskWordPageResponse;
import com.huly.backend.infrastructure.presentation.dto.riskWord.RiskWordRequest;
import com.huly.backend.infrastructure.presentation.dto.riskWord.RiskWordResponse;
import com.huly.backend.infrastructure.presentation.mapper.riskWord.RiskWordPresentationMapper;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * Controlador REST que expone los endpoints de gestión de palabras de riesgo emocional.
 * Cada método delega en su caso de uso correspondiente y aplica el mapeo entre los DTOs
 * web y los DTOs de dominio a través de {@link RiskWordPresentationMapper}.
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
    private final RiskWordPresentationMapper riskWordPresentationMapper;

    /**
     * Crea una nueva palabra de riesgo.
     *
     * @param request datos de la palabra a crear, validados automáticamente
     * @return {@code 201 Created} con la palabra creada en el cuerpo
     */
    @PostMapping
    public ResponseEntity<RiskWordResponse> create(@Valid @RequestBody RiskWordRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(
                riskWordPresentationMapper.toResponse(
                        createRiskWordUseCase.execute(riskWordPresentationMapper.toCreateRequest(request))));
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
        return ResponseEntity.ok(
                riskWordPresentationMapper.toResponse(
                        updateRiskWordUseCase.execute(riskWordPresentationMapper.toUpdateRequest(id, request))));
    }

    /**
     * Elimina una palabra de riesgo por su identificador.
     *
     * @param id identificador de la palabra a eliminar
     * @return {@code 204 No Content} si la eliminación fue exitosa
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        deleteRiskWordUseCase.execute(riskWordPresentationMapper.toDeleteRequest(id));
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
        return ResponseEntity.ok(
                riskWordPresentationMapper.toPageResponse(
                        listRiskWordsUseCase.execute(
                                riskWordPresentationMapper.toListRequest(word, active, severity, page, size))));
    }
}
