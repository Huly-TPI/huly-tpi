package com.huly.backend.domain.service;

import com.huly.backend.domain.model.RiskWord;
import com.huly.backend.domain.model.enums.RiskSeverity;
import com.huly.backend.domain.repository.RiskWordRepository;
import com.huly.backend.exception.BadRequestException;
import com.huly.backend.exception.ConflictException;
import com.huly.backend.exception.NotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

/**
 * Servicio de dominio que contiene la lógica de negocio para la gestión
 * de palabras de riesgo emocional. Centraliza las validaciones de unicidad,
 * existencia y severidad antes de delegar la persistencia al repositorio.
 */
@Service
@RequiredArgsConstructor
public class RiskWordService {

    private final RiskWordRepository riskWordRepository;

    /**
     * Crea una nueva palabra de riesgo verificando que no exista previamente.
     *
     * @param riskWord palabra de riesgo a crear
     * @return la palabra de riesgo persistida con su id asignado
     * @throws ConflictException si ya existe una palabra con el mismo valor
     */
    public RiskWord create(RiskWord riskWord) {
        assertWordIsUnique(riskWord.getWord());
        return riskWordRepository.save(riskWord);
    }

    /**
     * Actualiza los campos de una palabra de riesgo existente.
     *
     * @param id       identificador de la palabra a actualizar
     * @param riskWord objeto con los nuevos valores a aplicar
     * @return la palabra de riesgo actualizada y persistida
     * @throws NotFoundException si no existe una palabra con ese id
     * @throws ConflictException si el nuevo valor ya pertenece a otro registro
     */
    public RiskWord update(Long id, RiskWord riskWord) {
        RiskWord existing = findOrThrow(id);
        assertWordNotTakenByAnother(riskWord.getWord(), existing.getWord(), id);
        existing.setWord(riskWord.getWord());
        existing.setDescription(riskWord.getDescription());
        existing.setSeverity(riskWord.getSeverity());
        return riskWordRepository.save(existing);
    }

    /**
     * Elimina físicamente una palabra de riesgo por su identificador.
     *
     * @param id identificador de la palabra a eliminar
     * @throws NotFoundException si no existe una palabra con ese id
     */
    public void delete(Long id) {
        existsOrThrow(id);
        riskWordRepository.deleteById(id);
    }

    /**
     * Lista palabras de riesgo con filtros opcionales y paginación.
     *
     * @param word     fragmento para filtrar por contenido (puede ser {@code null})
     * @param active   estado activo/inactivo (puede ser {@code null})
     * @param severity nivel de severidad como texto (puede ser {@code null})
     * @param pageable configuración de paginación y ordenamiento
     * @return página de palabras de riesgo que coinciden con los filtros
     * @throws BadRequestException si el valor de severidad no corresponde a un nivel válido
     */
    public Page<RiskWord> list(String word, Boolean active, String severity, Pageable pageable) {
        validateSeverity(severity);
        return riskWordRepository.findAll(word, active, severity, pageable);
    }

    /**
     * Busca una palabra de riesgo por id y la retorna, o lanza una excepción si no existe.
     *
     * @param id identificador a buscar
     * @return la palabra de riesgo encontrada
     * @throws NotFoundException si no existe ningún registro con ese id
     */
    private RiskWord findOrThrow(Long id) {
        return riskWordRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Palabra de riesgo no encontrada con id: " + id));
    }

    /**
     * Verifica que exista una palabra de riesgo con el id dado, sin cargar la entidad.
     *
     * @param id identificador a verificar
     * @throws NotFoundException si no existe ningún registro con ese id
     */
    private void existsOrThrow(Long id) {
        if (!riskWordRepository.existsById(id)) {
            throw new NotFoundException("Palabra de riesgo no encontrada con id: " + id);
        }
    }

    /**
     * Verifica que el valor de la palabra no esté ya registrado en el sistema.
     *
     * @param word valor a verificar
     * @throws ConflictException si ya existe una palabra con ese valor
     */
    private void assertWordIsUnique(String word) {
        if (riskWordRepository.existsByWord(word)) {
            throw new ConflictException("Ya existe una palabra de riesgo con el valor: " + word);
        }
    }

    /**
     * Verifica que el nuevo valor no esté tomado por otro registro distinto al actual.
     * Si la palabra no cambió (comparación insensible a mayúsculas), omite la verificación.
     *
     * @param newWord     nuevo valor propuesto para la palabra
     * @param currentWord valor actual de la palabra en el registro existente
     * @param id          identificador del registro que se está editando
     * @throws ConflictException si otro registro ya usa el nuevo valor
     */
    private void assertWordNotTakenByAnother(String newWord, String currentWord, Long id) {
        if (!currentWord.equalsIgnoreCase(newWord) && riskWordRepository.existsByWordAndIdNot(newWord, id)) {
            throw new ConflictException("Ya existe una palabra de riesgo con el valor: " + newWord);
        }
    }

    /**
     * Valida que el texto de severidad corresponda a un valor del enum {@link RiskSeverity}.
     * Si el parámetro es {@code null}, la validación se omite sin error.
     *
     * @param severity texto a validar
     * @throws BadRequestException si el texto no coincide con ningún valor del enum
     */
    private void validateSeverity(String severity) {
        if (severity == null) return;
        try {
            RiskSeverity.valueOf(severity.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new BadRequestException("Severidad inválida. Valores aceptados: LOW, MEDIUM, HIGH");
        }
    }
}
